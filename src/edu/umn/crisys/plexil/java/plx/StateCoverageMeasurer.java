package edu.umn.crisys.plexil.java.plx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.values.NodeState;
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

	@Override
	public void quiescenceReached(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

	@Override
	public void endOfMicroStep(JavaPlan plan) {
		collectCoverage((PlexilTestable) plan);
	}

}
