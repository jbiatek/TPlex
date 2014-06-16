package edu.umn.crisys.plexil.il.optimize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;

public class RemoveDeadTransitions {
	
	private RemoveDeadTransitions() {}
	
	public static void optimize(Plan ilPlan) {
		for (NodeStateMachine nsm : ilPlan.getMachines()) {
			removeImpossibleTransitions(nsm);
		}
	}
	
    /**
     * Takes a list of transitions, and removes:
     * <ul>
     *      <li>Transitions that are never taken because a guard <code>isNeverTaken()</code></li>
     *      <li>Transitions that are never taken because a higher priority 
     *          transition <code>isAlwaysTaken()</code></li>
     * </ul>
     * 
     * @param chart The list that will be modified
     */
    private static void removeImpossibleTransitions(NodeStateMachine nsm) {
    	List<Transition> chart = nsm.transitions;
        // These optimizations are generic. We look for:
        //   - Transitions that are never taken
        //   - Transitions that are always taken that cause others to never be taken
        Map<State, Integer> alwaysTaken = new HashMap<State, Integer>();
        for (Iterator<Transition> it = chart.iterator();
                it.hasNext();) {
            Transition t = it.next();
            
            if (t.isNeverTaken()) {
                it.remove();
            } else if (t.isAlwaysTaken()) {
                // Is this the highest priority "always true" one that we've seen?
                if (alwaysTaken.containsKey(t.start)) {
                    if (alwaysTaken.get(t.start) > t.priority ) {
                        // This one's higher. Mark it.
                        alwaysTaken.put(t.start, t.priority);
                    }
                } else {
                    // First one we've seen. Mark it.
                    alwaysTaken.put(t.start, t.priority);
                }
            }
        }
        // Check remaining transitions to make sure they're not shadowed
        for (Iterator<Transition> it = chart.iterator();
                it.hasNext();) {
            Transition t = it.next();
            if (alwaysTaken.containsKey(t.start) &&
                    t.priority > alwaysTaken.get(t.start)) {
                // This one's going to be shadowed.
                it.remove();
            } 
        }
        
        // Lastly, find and keep all transitions that are reachable from the
        // starting state. 
        
        Set<Transition> reachableTransitions = new HashSet<Transition>();
        Set<State> reachableStates = new HashSet<State>();
        // For now, it's just the first state that we initially start in. 
        reachableStates.add(nsm.states.get(0));
        int previousSize = 0;
        do {
        	previousSize = reachableTransitions.size();
        	
        	for (Transition t : nsm.transitions) {
        		if (reachableStates.contains(t.start)) {
        			reachableStates.add(t.end);
        			reachableTransitions.add(t);
        		}
        	}
        	
        	
        } while (previousSize != reachableTransitions.size());
        
        nsm.transitions.retainAll(reachableTransitions);
        nsm.states.retainAll(reachableStates);
        
    }

}
