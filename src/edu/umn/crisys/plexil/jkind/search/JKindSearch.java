package edu.umn.crisys.plexil.jkind.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import concatenation.CreateHistoryVisitor;
import edu.umn.crisys.plexil.il2lustre.PlanToLustre;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.util.Util;
import enums.Generation;
import enums.Simulation;
import jkind.SolverOption;
import jkind.api.results.JKindResult;
import jkind.lustre.Node;
import jkind.lustre.Program;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import jkind.lustre.values.Value;
import jkind.translation.RemoveEnumTypes;
import jkind.translation.Translate;
import lustre.LustreTrace;
import simulation.LustreSimulator;
import testsuite.FillNullValues;

/**
 * Class for performing incremental search with JKind. Use the provided 
 * abstract methods to do something with the tests that are generated, like
 * write them to a file or do a compliance test on them. 
 * @author jbiatek
 *
 */
public abstract class JKindSearch {

	private Program lustreProgram;
	private PlanToLustre translator;
	private Set<TraceProperty> requestedGoals = new HashSet<>();
	private Set<TraceProperty> unmetGoals = new HashSet<>();
	private Set<TraceProperty> impossibleGoals = new HashSet<>();
	private Set<IncrementalTrace> chosenTraces = new HashSet<>();
	private Set<IncrementalTrace> redundantTraces = new HashSet<>();
	private Set<TraceProperty> nonPrefixAlreadyRun = new HashSet<>();
	private JKindSettings jkind;
	
	private boolean doIncrementalSearch = true;
	private ForkJoinPool workQueue;
	
	
	public JKindSearch(PlanToLustre translator) {
		this.translator = translator;
		System.out.println("Re-translating to Lustre");
		this.lustreProgram = translator.toLustre();
		JKindSettings.staticCheckLustreProgram(lustreProgram, SolverOption.Z3);
	}
	
	public void turnOffIncrementalSearch() {
		doIncrementalSearch = false;
	}
	
	public Set<TraceProperty> getRequestedGoals() {
		return requestedGoals;
	}

	public Set<TraceProperty> getUnmetGoals() {
		return unmetGoals;
	}

	public Set<IncrementalTrace> getChosenTraces() {
		return chosenTraces;
	}

	public Set<IncrementalTrace> getRedundantTraces() {
		return redundantTraces;
	}

	public Set<TraceProperty> getNonPrefixAlreadyRun() {
		return nonPrefixAlreadyRun;
	}

	public void go() {
		go(new JKindSettings(Integer.MAX_VALUE, 20));
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
	
	private void queueSearchForOnePropAtATime(JKindTestRun run) {
		if ( ! run.getPropertiesWithoutPrefix().isEmpty()) {
			for (TraceProperty p : run.getPropertiesWithoutPrefix()) {
				workQueue.execute(() -> {
					Program init = simplify(addProperties(lustreProgram, 
							Arrays.asList(p), translator));
					JKindSettings.staticCheckLustreProgram(init, SolverOption.Z3);
					JKindResult result = jkind.execute(init, System.out);
					fileResults(Optional.empty(), init, result, Arrays.asList(p));				
				});
			}
		}
	}

	
	public void go(JKindSettings settings) {
		// TODO This is just a test method.
		jkind = settings;
		
		// Prepare the work queue
		workQueue = new ForkJoinPool();
		// But don't start searching just yet -- we'll do a single search in
		// this thread first. That might be enough.
		boolean oldIncrementalSearchValue = doIncrementalSearch;
		doIncrementalSearch = false;
		
		// Run the "worker thread" in this thread, with a search that just tries
		// everything. 
		workerThread(generateMonolithicSearchStep());
		
		// Set search flag back to how it was
		doIncrementalSearch = oldIncrementalSearchValue;
		// If we're not incrementally searching, there's nothing more to do
		if ( ! doIncrementalSearch) {
			System.out.println("Incremental search is disabled, search is finished.");
			return;
		}
		// Did we find everything?
		if (unmetGoals.isEmpty()) {
			System.out.println("All search goals were found, search is finished.");
			return;
		}
		// Nope. Let's start searching incrementally.
		for (IncrementalTrace foundTrace : chosenTraces) {
			addTestCaseExtensionToQueue(foundTrace);
		}
		
		
		
		System.out.println("Main thread now sleeping until work queue empties.");
		// We can now wait until the work queue is emptied
		boolean keepGoing = ! workQueue.isQuiescent();
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
	
	
	synchronized private void addTestCaseExtensionToQueue(IncrementalTrace trace) {
		JKindTestRun testRun = new JKindTestRun();
		// Do any properties see stuff to do for this trace?
		unmetGoals.forEach(goal -> {
			if ( ! impossibleGoals.contains(goal) 
					&& trace.propertyHasntBeenTriedYet(goal) 
					&& goal.traceLooksReachable(trace)) {
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

	private synchronized void fileSuccess(IncrementalTrace foundTrace, LustreTrace extendedTrace) {
		boolean newGoalWasFound = unmetGoals.stream()
			.anyMatch(unmet -> foundTrace.getProperties().contains(unmet));
		if (newGoalWasFound) {
			chosenTraces.add(foundTrace);
			newGoalFound(foundTrace, extendedTrace);
		} else { 
			redundantTraces.add(foundTrace);
			redundantGoalFound(foundTrace, extendedTrace);
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
		if (newGoalWasFound && doIncrementalSearch) {
			addTestCaseExtensionToQueue(foundTrace);
		}
	}
	
	private synchronized void fileFailure(Optional<IncrementalTrace> prefix, TraceProperty prop) {
		if (prefix.isPresent()) {
			prefix.get().addAsFailure(prop);
		} else {
			nonPrefixAlreadyRun.add(prop);
		}
		noTestFound(prefix, prop);
	}

	private synchronized void fileImpossible(TraceProperty prop) {
		// The solver proved that this property is never true under initial
		// conditions. Don't look for it any more, and file it as such.
		unmetGoals.remove(prop);
		impossibleGoals.add(prop);
		goalFoundToBeImpossible(prop);
	}
	
	public abstract void newGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace);
	
	public abstract void redundantGoalFound(IncrementalTrace foundTrace, LustreTrace extendedTrace);
	
	public abstract void noTestFound(Optional<IncrementalTrace> prefix, TraceProperty property);
	
	public abstract void goalFoundToBeImpossible(TraceProperty property);
	
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
			JKindSettings.staticCheckLustreProgram(init, SolverOption.Z3);
			JKindResult result = jkind.execute(init, System.out);
			fileResults(Optional.empty(), init, result, run.getPropertiesWithoutPrefix());
		}
	}

	private void searchPrefix(IncrementalTrace prefix, Set<TraceProperty> propertiesToTry) {
		if (propertiesToTry.isEmpty()) return;
		// Start from the end of the prefix, and add properties specified
		// in the test run
		Program prog = historify(
				simplify(addProperties(lustreProgram, propertiesToTry, translator)), 
				prefix.getFullTrace());
		
		JKindSettings.staticCheckLustreProgram(prog, SolverOption.Z3);
		JKindResult result = jkind.execute(prog, System.out);
		fileResults(Optional.of(prefix), prog, result, propertiesToTry);
	}
	
	private void printNullValueReport(String name, LustreTrace trace) {
		if (trace == null) {
			return;
		}
		System.out.println("Here's the null value report for "+name+":");
		
		int nullValues = 0;
		int finalStepNullValues = 0;
		int total = 0;
		for (String var : trace.getVariableNames()) {
			for (int i=0; i < trace.getLength(); i++) {
				Value value = trace.getVariable(var).getValue(i);
				total++;
				if (value == null) {
					nullValues++;
					if (i + 1 == trace.getLength()) {
						finalStepNullValues++;
					}
				}
			}
		}
		System.out.println(nullValues+"/"+total+" values were null. ");
		System.out.println(finalStepNullValues+"/"+trace.getVariableNames().size()
				+" values were null in the final step.");
	}
	
	private void fileResults(Optional<IncrementalTrace> prefix, 
			Program lustre,
			JKindResult result, 
			Collection<TraceProperty> attemptedProps) {
		int found = 0;
		int notFound = 0;
		
//		for (Entry<String, LustreTrace> e : result.entrySet()) {
//			printNullValueReport(e.getKey(), e.getValue());
//		}
		Map<String, LustreTrace> tests = JKindSettings.getTestCases(lustre, result);
		Set<String> impossible = JKindSettings.getUntestableProperties(result);
		
		for (TraceProperty prop : attemptedProps) {
			// Did we get this one? 
			if (tests.containsKey(prop.getPropertyId())
					&& tests.get(prop.getPropertyId()) != null) {
				// Yes we did.
				IncrementalTrace foundTrace = new IncrementalTrace(
						tests.get(prop.getPropertyId()), 
						prefix,
						Util.asHashSet(prop));
				LustreTrace reEnumed = reEnumAndExtend(
						foundTrace.getFullTrace(), lustreProgram);
				fileSuccess(foundTrace, reEnumed);
				found++;
			} else if (impossible.contains(prop.getPropertyId())
					&&  ! prefix.isPresent()) {
				fileImpossible(prop);
			} else {
				// This one wasn't reachable from here, probably due to a
				// timeout. Make sure we don't try this again.
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
			simulated = sim.simulate(filled, Simulation.COMPLETE, sim.getAllVars());
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
