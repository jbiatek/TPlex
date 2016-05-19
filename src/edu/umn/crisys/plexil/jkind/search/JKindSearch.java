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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	private Set<IncrementalTrace> chosenTraces = new HashSet<>();
	private Set<IncrementalTrace> redundantTraces = new HashSet<>();
	private Set<TraceProperty> nonPrefixAlreadyRun = new HashSet<>();
	private JKindSettings jkind;
	
	private ForkJoinPool workQueue;
	
	private int fileNameCounter = 0;
	private File outDir;
	
	public JKindSearch(PlanToLustre translator, File outputDir) {
		this.translator = translator;
		outDir = outputDir;
		System.out.println("Re-translating to Lustre");
		this.lustreProgram = translator.toLustre();
		StaticAnalyzer.check(lustreProgram, SolverOption.Z3);
	}
	
	public void go() {
		go(JKindSettings.createBMCOnly(Integer.MAX_VALUE, 20));
	}
	
	public void addNodeExecutesNoParentFailObligations() {
		System.out.println("Adding goals to execute each node with no failures");
		// Try to execute each node, but not the root
		translator.getILPlan().getOriginalHierarchy().getChildren().stream()
			.forEach(child -> child.streamEntireHierarchy()
					.forEach(node -> addGoal(
							new NodeExecutesNoParentFailProperty(node))));
	}
	
	public void addTransitionCoverageObligations() {
		translator.getILPlan().getMachines()
			.forEach(nsm -> {
				nsm.getTransitions().forEach(t -> {
					addGoal(new SpecificTransitionProperty(t, nsm));
				});
			});
	}
	
	public void go(JKindSettings settings) {
		// TODO This is just a test method.
		jkind = settings;
		
		// Prepare the work queue
		workQueue = new ForkJoinPool();
		
		// We want to just run the initial goals in this thread, it'll spin off
		// new threads. 
		workerThread(generateMonolithicSearchStep());
		System.out.println("Main thread now sleeping until work queue empties.");
		// We can now wait until the work queue is emptied
		boolean keepGoing = true;
		while (keepGoing) {
			try {
				Thread.sleep(30 * 1000);
				if (workQueue.isQuiescent()) {
					System.out.println("Work queue has no more tasks");
					workQueue.shutdown();
					keepGoing = false;
				}
			} catch (InterruptedException e) {
				workQueue.shutdownNow();
				throw new RuntimeException(e);
			}
		}
		
		System.out.println("Didn't meet "+unmetGoals.size()+" goals out of "
				+requestedGoals.size()+". Met goals were:");
		requestedGoals.stream()
			.filter(goal -> !unmetGoals.contains(goal))
			.forEach(System.out::println);
		
		System.out.println("Unmet goals were:");
		unmetGoals.forEach(System.out::println);
	}

	public void addGoal(TraceProperty p) {
		requestedGoals.add(p);
		unmetGoals.add(p);
	}
	
	synchronized public JKindTestRun generateMonolithicSearchStep() {
		JKindTestRun ret = new JKindTestRun();
		// Ask each property to find something to do
		unmetGoals.forEach(unmetProperty -> {
			addToTestRun(unmetProperty, ret);
		});
		
		return ret;
	}
	
	synchronized private void writeTraceToFile(IncrementalTrace trace, LustreTrace reEnumed) {
		outDir.mkdir();
		String props = trace.getProperties().stream()
				.map(TraceProperty::toString)
				.map(str -> str.replaceAll("[<>]", ""))
				.map(str -> str.replace(' ', '_'))
				.collect(Collectors.joining("."));

		FileWriter fw = null;
		try { 
			File out = new File(outDir, "trace"+fileNameCounter+"-"+props+"-hash-"
					+trace.hashCode()+".csv");
			fileNameCounter++;
			fw = new FileWriter(out);
			fw.write(reEnumed.toString());
			fw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 

	}
	
	synchronized private void addToWorkQueue(IncrementalTrace trace) {
		JKindTestRun testRun = new JKindTestRun();
		// Do any properties see stuff to do for this trace?
		unmetGoals.forEach(goal -> {
			if (trace.propertyHasntBeenTriedYet(goal) && 
					goal.traceLooksReachable(trace)) {
				testRun.add(trace, goal);
			} 
			goal.createIntermediateGoalsFrom(trace, translator).stream()
				.filter(trace::propertyHasntBeenTriedYet)
				.forEach(prop -> testRun.add(trace, prop));
		});
		if (testRun.isCompletelyEmpty()) return;
		workQueue.execute(() -> workerThread(testRun));
	}
	
	synchronized private void addToTestRun(TraceProperty unmetProperty, JKindTestRun ret) {
		System.out.println("Trying property "+unmetProperty);
		// Ask it for anything it wants to try without using history
		Set<TraceProperty> fromScratch = unmetProperty.createInitialGoals();
		fromScratch.removeIf(goal -> nonPrefixAlreadyRun.contains(goal));
		if ( ! fromScratch.isEmpty()) {
			System.out.println("It wants to try "+fromScratch.size()+" new goals without prefixes.");
		}
		fromScratch.forEach(ret::add);
		
		// What traces haven't we been tried against already? 
		Set<IncrementalTrace> tracesToTry = chosenTraces.stream()
			.filter(trace -> trace.propertyHasntBeenTriedYet(unmetProperty))
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
	}

	private synchronized void fileSuccess(IncrementalTrace foundTrace, LustreTrace reEnumedTrace) {
		boolean newGoalWasFound = unmetGoals.stream()
			.anyMatch(unmet -> foundTrace.getProperties().contains(unmet));
		if (newGoalWasFound) {
			chosenTraces.add(foundTrace);
			writeTraceToFile(foundTrace, reEnumedTrace);
		} else { 
			redundantTraces.add(foundTrace);
		}
		// Anything that this found is no longer unmet.
		unmetGoals.removeAll(foundTrace.getProperties());
		
		// Tell the parent trace (if any) that we have tried these goals.
		if (foundTrace.getPrefix().isPresent()) {
			foundTrace.getPrefix().get().addAsSuccess(foundTrace);
		} else {
			nonPrefixAlreadyRun.addAll(foundTrace.getProperties());
		}
		
		// Now that all the bookkeeping is done, if this was a new thing
		// try to extend it.
		if (newGoalWasFound) {
			addToWorkQueue(foundTrace);
		}
	}
	
	private synchronized void fileFailure(Optional<IncrementalTrace> prefix, TraceProperty prop) {
		if (prefix.isPresent()) {
			prefix.get().addAsFailure(prop);
		} else {
			nonPrefixAlreadyRun.add(prop);
		}
	}
	
	
	public void workerThread(JKindTestRun run) {
		searchAllNoPrefix(run);
		run.getAllPrefixes().forEach(prefix -> 
			searchPrefix(prefix, run.getPropertiesForPrefix(prefix)));
	}


	private void searchAllNoPrefix(JKindTestRun run) {
		if ( ! run.getPropertiesWithoutPrefix().isEmpty()) {
			// Yes. Add the properties and go.
			Program init = simplify(addProperties(lustreProgram, 
					run.getPropertiesWithoutPrefix(), translator));
			StaticAnalyzer.check(init, SolverOption.Z3);
			Map<String, LustreTrace> result = jkind.execute(init, System.out);
			fileResults(Optional.empty(), result, run.getPropertiesWithoutPrefix());
		}
	}

	private void searchPrefix(IncrementalTrace prefix, Set<TraceProperty> propertiesToTry) {
		if (propertiesToTry.isEmpty()) return;
		// Start from the end of the prefix, and add properties specified
		// in the test run
		Program prog = historify(
				simplify(addProperties(lustreProgram, propertiesToTry, translator)), 
				prefix.getFullTrace());
		
		StaticAnalyzer.check(prog, SolverOption.Z3);
		Map<String, LustreTrace> result = jkind.execute(prog, System.out);
		fileResults(Optional.of(prefix), result, propertiesToTry);
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
						prefix,
						Util.asHashSet(prop));
				LustreTrace reEnumed = reEnumAndExtend(
						foundTrace.getFullTrace(), lustreProgram);
				fileSuccess(foundTrace, reEnumed);
				found++;
			} else {
				// Can't get this one. Don't try this combination again.
				fileFailure(prefix, prop);
				notFound++;
			}
		}
		System.out.println("Logged results for "
				+prefix.map(t -> "prefix "+t).orElse("run with no prefix"));
		System.out.println("Found "+found+" new traces, didn't find "+notFound+".");
		System.out.println("Current progress: ");
		System.out.println("Have found "+chosenTraces.size()+" total traces.");
		System.out.println("There are "+unmetGoals.size()+" goals remaining.");
	}

	

	/**
	 * Using the given LustreTrace test case, which has been run through the
	 * simulator and therefore is in terms of integers, do two things:
	 * 
	 * First, simulate it again, but this time add 10 more steps where the 
	 * inputs don't change. This allows a human to see one way that the plan
	 * was "going". If it immediately hits a dead end, it could explain why
	 * a property couldnt' be found.
	 * 
	 * Second, this method also transforms the integers back into enumerated
	 * values, making it possible for a human to read and also making it 
	 * runnable on the original Lustre source code. 
	 * 
	 * @param trace
	 * @param lustreProgram
	 * @return
	 */
	private static LustreTrace reEnumAndExtend(LustreTrace trace, Program lustreProgram) {
		// We want to let the test case run a little more to give an idea
		// of where it was going. Also, this will give us the full trace
		// instead of just inputs, and those inputs will be enum names
		// instead of raw ints. 
		return JKindResultUtils.extendTestCase(trace, lustreProgram, 10);
	}

	/**
	 * Perform the history transformation on this Program. The returned Program
	 * will be equivalent to the state of the Program after the given 
	 * LustreTrace has been run.  
	 * 
	 * The given Program must be ready to be simulated: It cannot have 
	 * enumerated values in it. Use the simplify() method to do this. 
	 * 
	 * @param simplified
	 * @param trace
	 * @return
	 */
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
		
		// History transformation removes properties, so add them back
		NodeBuilder nb = new NodeBuilder(history);
		nb.addProperties(simplified.getMainNode().properties);
		
		return duplicateWithNewMainNode(simplified, nb.build());
	}
	
	/**
	 * Create a new Program that's a clone of this one, but with the given
	 * trace properties added to it. 
	 * @param p
	 * @param props
	 * @param translator
	 * @return
	 */
	private static Program addProperties(Program p, Collection<TraceProperty> props,
			PlanToLustre translator) {
		NodeBuilder nb = new NodeBuilder(p.getMainNode());
		props.forEach(property -> property.addProperty(nb, translator));
		return duplicateWithNewMainNode(p, nb.build());
	}
	
	/**
	 * Take an existing Program and clone it, swapping out the main node for
	 * a new one (for our purpose, probably a similar node with different 
	 * properties.)
	 * 
	 * @param old
	 * @param newNode
	 * @return
	 */
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
	
	/**
	 * Remove enumerated types from this Program, turning them into integers.
	 * The simulator requires programs to be preprocessed this way. 
	 * 
	 * @param p
	 * @return
	 */
	private static Program simplify(Program p) {
		return new Program(RemoveEnumTypes.node(Translate.translate(p)));
	}
	
	
}
