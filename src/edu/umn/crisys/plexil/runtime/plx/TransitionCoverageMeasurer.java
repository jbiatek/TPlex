package edu.umn.crisys.plexil.runtime.plx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.simulator.ILSimObserver;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class TransitionCoverageMeasurer implements ILSimObserver {

	private Set<Transition> coveredTransitions = new HashSet<>();
	private Set<Transition> missingTransitions = new HashSet<>();
	
	public TransitionCoverageMeasurer(Plan ilPlan) {
		ilPlan.getMachines().forEach(nsm -> {
			missingTransitions.addAll(nsm.getTransitions());
		});
	}

	@Override
	public void transitionTaken(Transition t) {
		coveredTransitions.add(t);
		missingTransitions.remove(t);
	}
	
	
	public void printData() {
		System.out.println("Transition coverage information");
		System.out.println("-------------------------------\n");
		System.out.println("\n"+coveredTransitions.size()+" transitions were covered");
		System.out.println(missingTransitions+" transitions were not covered");
		
		Map<NodeState,Integer> origin = new HashMap<>();
		Map<NodeState,Integer> destination = new HashMap<>();
		Map<NodeUID,Integer> covered = new HashMap<>();
		Map<NodeUID,Integer> uncovered = new HashMap<>();
		Set<NodeUID> allUids = new HashSet<>();

		missingTransitions.forEach(t -> {
			t.start.tags.forEach((uid, state) -> {
				// Add 1 for each tagged start state
				origin.put(state, 
						origin.getOrDefault(state, 0) + 1);
				uncovered.put(uid, uncovered.getOrDefault(uid, 0) + 1);
				allUids.add(uid);
				});
			t.end.tags.forEach((uid, state) -> {
				destination.put(state, 
						destination.getOrDefault(state, 0) + 1);
				});
		});
		coveredTransitions.forEach(t -> {
			t.start.tags.forEach((uid, state) -> {
				covered.put(uid, covered.getOrDefault(uid, 0) + 1);
				allUids.add(uid);
			});
		});
		
		
		System.out.println("Uncovered transitions originating in:");
		origin.forEach((state, num) -> {
			System.out.println(state +": "+num);
		});
		System.out.println("Uncovered transitions ending in:");
		destination.forEach((state, num) -> {
			System.out.println(state +": "+num);
		});
		System.out.println("By Node UID: ");
		allUids.forEach( uid -> {
			System.out.println(uid+": "+covered.get(uid)+" covered, "
					+uncovered.get(uid)+" uncovered");
		});
	}
}
