package edu.umn.crisys.plexil.il.optimize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;

public class RemoveDeadTransitions {
	
	private RemoveDeadTransitions() {}
	
	public static void optimize(Plan ilPlan) {
		for (NodeStateMachine nsm : ilPlan.getMachines()) {
			removeImpossibleTransitions(nsm.transitions);
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
    private static void removeImpossibleTransitions(List<Transition> chart) {
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
    }

}
