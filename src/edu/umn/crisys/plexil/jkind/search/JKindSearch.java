package edu.umn.crisys.plexil.jkind.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jkind.JKindExecution;
import jkind.SolverOption;
import jkind.analysis.StaticAnalyzer;
import jkind.lustre.Node;
import jkind.lustre.Program;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import jkind.translation.RemoveEnumTypes;
import jkind.translation.Translate;
import lustre.LustreTrace;
import simulation.LustreSimulator;
import testsuite.FillNullValues;
import concatenation.CreateHistoryVisitor;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.util.Util;
import enums.Generation;
import enums.Simulation;

public class JKindSearch {

	private Program lustreProgram;
	private PlanToLustre translator;
	private Set<TraceProperty> requestedGoals = new HashSet<>();
	private Set<TraceProperty> unmetGoals = new HashSet<>();
	private Set<IncrementalTrace> allTraces = new HashSet<>();
	private Set<TraceProperty> nonPrefixAlreadyRun = new HashSet<>();
	
	public JKindSearch(PlanToLustre translator) {
		this.translator = translator;
		System.out.println("Re-translating to Lustre");
		this.lustreProgram = translator.toLustre();
		StaticAnalyzer.check(lustreProgram, SolverOption.Z3);
	}
	
	public void go() {
		// TODO This is just a test method.
		JKindExecution.timeout = 240;
		//JKindExecution.iteration = 7;
		
		System.out.println("Adding goals to execute each node with no failures");
		// Try to execute each node, but not the root
		translator.getILPlan().getOriginalHierarchy().getChildren().stream()
			.forEach(child -> child.streamEntireHierarchy()
					.forEach(node -> addGoal(
							new NodeExecutesNoParentFailProperty(node))));
		
		for (int i = 0; true; i++) {
			if (unmetGoals.isEmpty()) break;
			System.out.println("Starting iteration "+(i+1));
			JKindTestRun testRun = generateNextSearchStep();
			if (testRun.isCompletelyEmpty()) {
				System.out.println("Found absolutely nothing new to search.");
				break;
			}
			search(testRun);
		}
		
		System.out.println("Didn't meet "+unmetGoals.size()+" goals out of "
				+requestedGoals.size()+". Met goals were:");
		requestedGoals.stream()
			.filter(goal -> !unmetGoals.contains(goal))
			.forEach(System.out::println);
		
		System.out.println("Dumping out traces to files.");
		System.out.println("The last input step will be duplicated 10 times so that you can see where the plan was probably going.");
		File outDir = new File("jkind-traces");
		outDir.mkdir();
		int i=0;
		for (IncrementalTrace trace : allTraces) {
			String props = trace.getProperties().stream()
					.map(TraceProperty::toString)
					.map(str -> str.replaceAll("[<>]", ""))
					.map(str -> str.replace(' ', '_'))
					.collect(Collectors.joining("."));
			// We want to let the test case run a little more to give an idea
			// of where it was going. Also, this will give us the full trace
			// instead of just inputs, and those inputs will be enum names
			// instead of raw ints. 
			LustreTrace reEnumed = JKindResultUtils
					.extendTestCase(trace.getFullTrace(), lustreProgram, 10);
			
			FileWriter fw = null;
			try { 
				File out = new File(outDir, "trace"+i+"-"+props+"-hash-"
						+trace.hashCode()+".csv");
				i++;
				fw = new FileWriter(out);
				fw.write(reEnumed.toString());
				fw.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 
		}
	}

	public void addGoal(TraceProperty p) {
		requestedGoals.add(p);
		unmetGoals.add(p);
	}
	
	public JKindTestRun generateNextSearchStep() {
		JKindTestRun ret = new JKindTestRun();
		// Ask each property to find something to do
		unmetGoals.forEach(unmetProperty -> {
			System.out.println("Trying property "+unmetProperty);
			// Ask it for anything it wants to try without using history
			Set<TraceProperty> fromScratch = unmetProperty.createInitialGoals();
			fromScratch.removeIf(goal -> nonPrefixAlreadyRun.contains(goal));
			if ( ! fromScratch.isEmpty()) {
				System.out.println("It wants to try "+fromScratch.size()+" new goals without prefixes.");
			}
			fromScratch.forEach(ret::add);
			
			// What traces haven't we been tried against already? 
			Set<IncrementalTrace> tracesToTry = allTraces.stream()
				.filter(trace -> trace.filterAlreadyDone(unmetProperty))
				.collect(Collectors.toSet());
			
			// The property thinks that these will work, so try them
			Set<IncrementalTrace> reachable = tracesToTry.stream()
				//.filter(unmetProperty::traceLooksReachable)
				.collect(Collectors.toSet());
			reachable.forEach(trace -> ret.add(trace, unmetProperty));
			System.out.println("It thinks that "+reachable.size()+" new traces are reachable.");
			
			/*if (reachable.isEmpty()) {
				// It didn't see anything that looked reachable. Let's get some
				// intermediate goals to try. 
				System.out.println("So instead we will look for intermediate goals.");
				
				int count = 0;
				for (IncrementalTrace trace : allTraces) {
					// Ask our property to try and search forward from here
					Set<TraceProperty> intermediate = 
							unmetProperty.createIntermediateGoalsFrom(trace, translator);
					intermediate.removeIf(property -> ! trace.filterAlreadyDone(property));
					intermediate.forEach(prop -> ret.add(trace, prop));
					count += intermediate.size();
				}
				System.out.println("Added "+count+" new intermediate goals.");
			}*/
		});
		
		// Almost done. Just remove any "initial" properties that we've already
		// tried.
		ret.removeAllFromWithoutPrefix(nonPrefixAlreadyRun);
		
		return ret;
	}
	
	private static Program historify(Program simplified, LustreTrace trace) {
		// Fill in null values
		LustreTrace filled = FillNullValues.fill(trace, simplified, Generation.DEFAULT);
		LustreSimulator sim = new LustreSimulator(simplified);
		LustreTrace simulated;
		try { 
			simulated = sim.simulate(filled, Simulation.COMPLETE);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(simplified);
			throw e;
		}

		// And add history
		Node history = CreateHistoryVisitor.node(simplified.getMainNode(), simulated);
		
		// History translation kills all properties, add them back in
		NodeBuilder nb = new NodeBuilder(history);
		nb.addProperties(simplified.getMainNode().properties);
		
		return duplicateWithNewMainNode(simplified, nb.build());
	}
	
	private Program addProperties(Program p, Collection<TraceProperty> props) {
		NodeBuilder nb = new NodeBuilder(p.getMainNode());
		props.forEach(property -> property.addProperty(nb, translator));
		return duplicateWithNewMainNode(p, nb.build());
	}
	
	private static Program duplicateWithNewMainNode(Program old, Node newNode) {
		ProgramBuilder pb = new ProgramBuilder();
		pb.addNode(newNode);
		pb.setMain(newNode.id);
		pb.addTypes(old.types);
		pb.addConstants(old.constants);
		for (Node n : old.nodes) {
			if (n.id.equals(old.main)) continue;
			pb.addNode(n);
		}
		return pb.build();
	}
	
	private synchronized void fileResults(Optional<IncrementalTrace> prefix, 
			Map<String,LustreTrace> result, Collection<TraceProperty> attemptedProps) {
		int found = 0;
		int notFound = 0;
		
		for (TraceProperty prop : attemptedProps) {
			// Did we get this one? 
			if (result.containsKey(prop.getPropertyId())
					&& result.get(prop.getPropertyId()) != null) {
				// Yes we did.
				IncrementalTrace foundTrace = new IncrementalTrace(
						result.get(prop.getPropertyId()), 
						prefix,
						Util.asHashSet(prop));
				allTraces.add(foundTrace);
				unmetGoals.remove(prop);
				if (prefix.isPresent()) {
					prefix.get().addAsSuccess(prop, foundTrace);
				} else {
					nonPrefixAlreadyRun.add(prop);
				}
				found++;
			} else {
				// Can't get this one. Don't try this combination again.
				if (prefix.isPresent()) {
					prefix.get().addAsFailure(prop);
				} else {
					nonPrefixAlreadyRun.add(prop);
				}
				notFound++;
			}
		}
		System.out.println("Logged results for "
				+prefix.map(t -> "prefix "+t).orElse("run with no prefix"));
		System.out.println("Found "+found+" new traces, didn't find "+notFound+".");
	}
	
	private static Program simplify(Program p) {
		return new Program(RemoveEnumTypes.node(Translate.translate(p)));
	}
	
	private void searchAllNoPrefix(JKindTestRun run) {
		if ( ! run.getPropertiesWithoutPrefix().isEmpty()) {
			// Yes. Add the properties and go.
			Program init = simplify(addProperties(lustreProgram, run.getPropertiesWithoutPrefix()));
			StaticAnalyzer.check(init, SolverOption.Z3);
			Map<String, LustreTrace> result = JKindExecution.execute(init);
			fileResults(Optional.empty(), result, run.getPropertiesWithoutPrefix());
		}
	}
	
	private void searchPrefix(IncrementalTrace prefix, Set<TraceProperty> propertiesToTry) {
		// Start from the end of the prefix, and add properties specified
		// in the test run
		Program prog = historify(
				simplify(addProperties(lustreProgram, propertiesToTry)), 
				prefix.getFullTrace());
		
		StaticAnalyzer.check(prog, SolverOption.Z3);
		Map<String, LustreTrace> result = JKindExecution.execute(prog);
		fileResults(Optional.of(prefix), result, propertiesToTry);
	}
	
	
	public void search(JKindTestRun run) {
		System.out.println("Current progress: ");
		System.out.println("Have found "+allTraces.size()+" total traces.");
		System.out.println("There are "+unmetGoals.size()+" goals remaining.");
		System.out.println("Next JKind execution run starting...");
		
		ExecutorService pool = Executors.newWorkStealingPool();
		
		// Search anything that uses the empty prefix 
		pool.execute(() -> searchAllNoPrefix(run));
		
		// Do a run for each prefix that we're trying to extend.
		/*
		run.getAllPrefixes().stream()
		.forEach(prefix -> pool.execute(() -> 
			searchPrefix(prefix, run.getPropertiesForPrefix(prefix))));
		*/
		run.getSortedByMostProperties(16)
			.forEach(entry -> {
				System.out.println("Going to extend trace "+entry.getKey()
					+" toward "+entry.getValue().size()+" goals.");
				pool.execute(() -> 
					searchPrefix(entry.getKey(), entry.getValue()));
			});
		
		
		// Wait for them all to be done
		pool.shutdown();
		try {
			while ( ! pool.awaitTermination(3, TimeUnit.MINUTES)) {};
		} catch (InterruptedException e) {
			pool.shutdownNow();
			throw new RuntimeException(e);
		}
		
	}
	
	
}
