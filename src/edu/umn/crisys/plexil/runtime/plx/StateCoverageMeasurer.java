package edu.umn.crisys.plexil.runtime.plx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.test.java.PlanState;
import edu.umn.crisys.plexil.test.java.PlexilTestable;

public class StateCoverageMeasurer implements JavaPlanObserver {
	
	private Map<NodeUID, Set<NodeState>> statesCovered = new HashMap<NodeUID, Set<NodeState>>();
	
	public void collectCoverage(PlexilTestable plan) {
		recursiveCollection(plan.getSnapshot());
	}
	
	private void recursiveCollection(PlanState state) {
		NodeState pState = (NodeState) state.getVarValue(".state");
		if (state.getUID() == null) throw new NullPointerException("No UID for "+state);
		if (pState == null) throw new NullPointerException("State variable is null in node "+state.getUID());
		
		if ( ! statesCovered.containsKey(state.getUID())) {
			statesCovered.put(state.getUID(), new HashSet<NodeState>());
		}
		statesCovered.get(state.getUID()).add(pState);
		
		for (PlanState child : state.getChildren() ) {
			recursiveCollection(child);
		}
		
	}
	
	public void printData() {
		System.out.println("State coverage information");
		System.out.println("--------------------------\n");
		
		// Sort the UIDs alphabetically to make it easier to read
		List<NodeUID> uids = new ArrayList<NodeUID>();
		uids.addAll(statesCovered.keySet());
		Collections.sort(uids, new Comparator<NodeUID>() {

			@Override
			public int compare(NodeUID o1, NodeUID o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		// Print data
		for (NodeUID uid : uids) {
			System.out.println(uid+":");
			StringBuilder states = new StringBuilder();
			
			// Sort the states too. 
			List<NodeState> sortedStates = new ArrayList<NodeState>();
			sortedStates.addAll(statesCovered.get(uid));
			Collections.sort(sortedStates);
			
			for (NodeState state : sortedStates) {
				states.append(state+" ");
			}
			System.out.println("    "+states);
		}
	}
	
	public int getNumStatesCovered() {
		int coverage = 0;
		for (NodeUID uid : statesCovered.keySet()) {
			coverage += statesCovered.get(uid).size();
		}
		return coverage;
	}
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

	@Override
	public void endOfMicroStepBeforeCommit(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

	@Override
	public void endOfExecution(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

}
