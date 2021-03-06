package edu.umn.crisys.plexil.il.statemachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;

public class NodeStateMachine {
    
	
	public Plan thePlan;
	
	/**
	 * The list of node IDs that this state machine is in charge of.
	 */
	private List<NodeUID> nodeIds = new LinkedList<NodeUID>(); 
	private String nsmId;
	
	private List<State> states = new ArrayList<State>();
	private List<Transition> transitions = new ArrayList<Transition>();

	public NodeStateMachine(NodeUID nId, Plan thePlan) { 
        nodeIds.add(nId) ;
        nsmId = nId.toString();
        this.thePlan = thePlan;
    }
	
	public void orderTransitionsByPriority() {
		Collections.sort(transitions);
	}
	
	public List<Transition> getTransitions() {
		return transitions;
	}

	public List<State> getStates() {
		return states;
	}
	
	public String getStateMachineId() {
		return nsmId;
	}
	
    public String toString() { 
    	return nodeIds.toString(); 
    }	
    
	public List<NodeUID> getNodeIds() { 
		return nodeIds ;
	}
	
	// methods for adding states and transitions
	public void addState(State s) {
	    s.stateMachine = this;
		states.add(s); 
	}
	
	public int indexOf(State s) {
	    return states.indexOf(s);
	}
	
	public String toLongString() {
	    String ret = "State machine for nodes: ";
	    for (NodeUID id : nodeIds) {
	        ret += id+", ";
	    }
	    ret += "\nState mappings: \n";
	    for (State s : states) {
	        ret += "  State "+indexOf(s)+": ";
	        for (NodeUID node : s.tags.keySet()) {
	            ret += node + " => " + s.tags.get(node) + ",\n";
	        }
	        ret += "    Entry Actions: \n";
	        ret += "        "+s.entryActions.stream()
	        		.map(Object::toString)
	        		.collect(Collectors.joining(", "))+"\n";
	        ret += "    In actions: \n";
	        ret += "        "+s.inActions.stream()
    				.map(Object::toString)
    					.collect(Collectors.joining(", "))+"\n";
	    }
	    ret += "\nTransitions: \n";
	    for (Transition t : transitions) {
	        ret += t.toString()+"\n";
	    }
	    
	    return ret;
	}
	
	public Transition addTransition(Transition t) {
	    transitions.add(t);
	    return t;
	}
	
	public Set<Transition> getHigherTransitionsThan(Transition theTransition) {
		if (! transitions.contains(theTransition)) {
			throw new RuntimeException("This is a foreign transition!");
		}
		Set<Transition> matches = new HashSet<Transition>();
		
		for (Transition t : transitions) {
			if (t.start == theTransition.start && t.priority < theTransition.priority ) {
				matches.add(t);
			}
		}
		return matches;
	}



}
