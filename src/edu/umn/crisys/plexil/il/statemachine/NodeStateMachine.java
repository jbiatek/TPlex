package edu.umn.crisys.plexil.il.statemachine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	        ret += "    State "+indexOf(s)+": ";
	        for (NodeUID node : s.tags.keySet()) {
	            ret += node + " => " + s.tags.get(node) + ", ";
	        }
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



}
