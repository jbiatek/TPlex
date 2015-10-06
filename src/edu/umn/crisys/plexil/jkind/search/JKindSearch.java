package edu.umn.crisys.plexil.jkind.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import edu.umn.crisys.util.Util;
import enums.Generation;
import enums.Simulation;

public class JKindSearch {

	private Program lustreProgram;
	private PlanToLustre translator;
	private Set<TraceProperty> requestedGoals = new HashSet<>();
	private Set<TraceProperty> unmetGoals = new HashSet<>();
	private Set<IncrementalTrace> allTraces = new HashSet<>();
	private Set<TraceProperty> nonPrefixedFailures = new HashSet<>();
	
	public JKindSearch(PlanToLustre translator) {
		this.translator = translator;
		System.out.println("Re-translating to Lustre");
		this.lustreProgram = translator.toLustre();
		StaticAnalyzer.check(lustreProgram, SolverOption.Z3);
	}
	
	public void go() {
		// TODO This is just a test method.
		JKindExecution.timeout = 240;
		//JKindExecution.iteration = 11;
		
		System.out.println("Adding goals to execute each node with no failures");
		// Try to execute each node
		translator.getILPlan().getOriginalHierarchy().streamEntireHierarchy()
		.forEach(node -> addGoal(new NodeExecutesNoParentFailProperty(node)));
		
		for (int i = 0; i < 5; i++) {
			if (unmetGoals.isEmpty()) break;
			System.out.println("Starting iteration "+(i+1));
			System.out.println("Have found "+allTraces.size()+" total traces.");
			System.out.println("There are "+unmetGoals.size()+" goals remaining.");
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
		
		System.out.println("Dumping out traces to files");
		File outDir = new File("jkind-traces");
		outDir.mkdir();
		int i=0;
		for (IncrementalTrace trace : allTraces) {
			String props = trace.getProperties().stream()
					.map(TraceProperty::toString)
					.map(str -> str.replaceAll("[<>]", ""))
					.map(str -> str.replace(' ', '_'))
					.collect(Collectors.joining("."));
			FileWriter fw = null;
			try { 
				File out = new File(outDir, "trace"+i+"-"+props+".csv");
				i++;
				fw = new FileWriter(out);
				fw.write(trace.getFullTrace().toString());
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
			fromScratch.removeIf(goal -> nonPrefixedFailures.contains(goal));
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
				.filter(unmetProperty::traceLooksReachable)
				.collect(Collectors.toSet());
			reachable.forEach(trace -> ret.add(trace, unmetProperty));
			System.out.println("It thinks that "+reachable.size()+" new traces are reachable.");
			
			if (reachable.isEmpty()) {
				// It didn't see anything that looked reachable. Let's get some
				// intermediate goals to try. 
				System.out.println("So instead, let's look for new intermediate goals.");
				allTraces.forEach( trace -> {
					// Ask our property to try and search forward from here
					Set<TraceProperty> intermediate = 
							unmetProperty.createIntermediateGoalsFrom(trace, translator);
					intermediate.removeIf(property -> ! trace.filterAlreadyDone(property));
					System.out.println("Found "+intermediate.size()+" intermediate goals for trace "+trace);
					intermediate.forEach(prop -> ret.add(trace, prop));
				});
			}
		});
		
		// Almost done. Just remove any "initial" properties that we've already
		// tried.
		ret.removeAllFromWithoutPrefix(nonPrefixedFailures);
		
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
	
	private void fileResults(Optional<IncrementalTrace> prefix, 
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
						Util.asHashSet(prop));
				allTraces.add(foundTrace);
				unmetGoals.remove(prop);
				if (prefix.isPresent()) {
					prefix.get().addAsSuccess(prop, foundTrace);
				}
				found++;
			} else {
				// Can't get this one. Don't try this combination again.
				if (prefix.isPresent()) {
					prefix.get().addAsFailure(prop);
				} else {
					nonPrefixedFailures.add(prop);
				}
				notFound++;
			}
		}
		System.out.println("Found "+found+" traces, didn't find "+notFound+".");
	}
	
	private static Program simplify(Program p) {
		return new Program(RemoveEnumTypes.node(Translate.translate(p)));
	}
	
	public void search(JKindTestRun run) {
		System.out.println("JKind execution run starting...");

		
		// Anything to run with no prefixes? 
		System.out.println("Searching goals with no prefix");
		if ( ! run.getPropertiesWithoutPrefix().isEmpty()) {
			// Yes. Add the properties and go.
			Program init = simplify(addProperties(lustreProgram, run.getPropertiesWithoutPrefix()));
			StaticAnalyzer.check(init, SolverOption.Z3);
			System.out.println("There are "+run.getPropertiesWithoutPrefix().size()+" properties.");
			Map<String, LustreTrace> result = JKindExecution.execute(init);
			fileResults(Optional.empty(), result, run.getPropertiesWithoutPrefix());
		}
		
		// Do a run for each prefix that we're trying to extend. 
		for (IncrementalTrace prefix : run.getAllPrefixes()) {
			// Start from the end of the prefix, and add properties specified
			// in the test run
			Set<TraceProperty> propertiesToTry = run.getPropertiesForPrefix(prefix);
			System.out.println("Trying prefix "+prefix);
			Program prog = historify(
					simplify(addProperties(lustreProgram, propertiesToTry)), 
					prefix.getFullTrace());
			System.out.println(propertiesToTry.size()+" properties on this prefix.");
			
			// TODO: Multithreading? 
			StaticAnalyzer.check(prog, SolverOption.Z3);
			Map<String, LustreTrace> result = JKindExecution.execute(prog);
			fileResults(Optional.of(prefix), result, propertiesToTry);
		}
	}
	
	
}
