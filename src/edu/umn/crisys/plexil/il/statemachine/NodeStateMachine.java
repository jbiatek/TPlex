package edu.umn.crisys.plexil.il.statemachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JSwitch;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.java.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.translator.il.NodeUID;
import edu.umn.crisys.plexil.translator.il.Plan;
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;
import edu.umn.crisys.plexil.translator.il.action.SetTimepointAction;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;

public class NodeStateMachine {
    
	
	public Plan thePlan;
	
	/**
	 * The list of node IDs that this state machine is in charge of.
	 */
	private List<NodeUID> nodeIds = new LinkedList<NodeUID>(); 
	public String nsmId;
	
	public List<State> states = new ArrayList<State>();
	public List<Transition> transitions = new ArrayList<Transition>();

	public NodeStateMachine(NodeUID nId, Plan thePlan) { 
        nodeIds.add(nId) ;
        nsmId = nId.toString();
        this.thePlan = thePlan;
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
