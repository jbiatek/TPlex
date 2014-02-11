package edu.umn.crisys.plexil.translator.il;

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
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;
import edu.umn.crisys.plexil.translator.il.action.SetTimepointAction;
import edu.umn.crisys.plexil.translator.il.vars.NodeStateReference;

public class NodeStateMachine {
    
	
	public Plan thePlan;
	
	/**
	 * The list of node IDs that this state machine is in charge of.
	 */
	public List<NodeUID> nodeIds = new LinkedList<NodeUID>(); 
	public String nsmId;
	
	public List<State> states = new ArrayList<State>();
	public List<Transition> transitions = new ArrayList<Transition>();

	public NodeStateMachine(NodeUID nId, Plan thePlan) { 
        nodeIds.add(nId) ;
        nsmId = nId.toString();
        this.thePlan = thePlan;
    }
    public String toString() { return nodeIds.toString(); }	
	public List<NodeUID> getNodeIds() { return nodeIds ; }
	
	// methods for adding states and transitions
	public void addState(State s) {
	    s.stateMachine = this;
		states.add(s); 
	}
	
	public int indexOf(State s) {
	    return states.indexOf(s);
	}
	
	public State lookupState(int stateId) {
		return states.get(stateId);
	}

	public List<Transition> lookupTransitions(State src, State dst) {
		List<Transition> tl = new LinkedList<Transition>(); 
		
		for (Transition t : transitions) {
			if (t.start == src && t.end == dst) {
				tl.add(t);
			}
		}
		
		return tl;
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
	
	/**
	 * Add a transition to this state chart. This takes NodeStates, not States,
	 * and is meant for adding in the "real" PLEXIL rules. In particular, it assumes
	 * that State 0 is INACTIVE, State 1 is WAITING, etc. Modifying/adding
	 * new higher level transitions is still TBD.
	 * @param priority
	 * @param start
	 * @param end
	 * @param guards
	 * @return the transition that was created, so that you can add actions to it.
	 */
/*	public Transition addTransition(NodeTranslateToIL node, int priority, NodeState start, NodeState end, TransitionGuard...guards) {
	    if (nodeIds.size() != 1) {
	        throw new RuntimeException("This method is only meant for simple nodes.");
	    }
	    
	    
	    State realStart = lookupState(start.ordinal());
	    State realEnd = lookupState(end.ordinal());
	    
	    String description = node.nodeId +" : "+start+" ("+priority+") -> "+end;
	    
	    Transition t = new Transition(description, priority, realStart, realEnd, guards);
	    // Add node timepoint assignment actions
	    // Our starting node has ended at this time
	    t.addAction(new SetTimepointAction(node.getTimepoint(start, NodeTimepoint.END)));
	    // Our destination node is now starting.
	    t.addAction(new SetTimepointAction(node.getTimepoint(end, NodeTimepoint.START)));
	    
	    transitions.add(t);
	    
	    return t;
	}*/
	
	public Transition addTransition(Transition t) {
	    transitions.add(t);
	    return t;
	}
	
    

	public String getStepMethodName() {
	    return NameUtils.clean("MicroStep___"+nsmId);
	}
	
	/*************************************************************
	 * 
	 * Methods relating to Java translation 
	 * 
	 **************************************************************/
	
	public void toJava(JDefinedClass clazz ) {
	    JCodeModel cm = clazz.owner();
	    
	    // Create the variable to hold the current state
	    JClass stateClass = cm.ref(SimpleCurrentNext.class).narrow(cm.ref(Integer.class));
	    JFieldVar stateVar = clazz.field(JMod.PRIVATE, stateClass, NameUtils.clean(nsmId+".state"), 
	            JExpr._new(stateClass).arg(JExpr.lit(0)));
	    
	    // Now we start making the step function.
	    JMethod stepMethod = clazz.method(JMod.NONE, cm.VOID, getStepMethodName());
	    stepMethod.body().decl(cm.ref(PBoolean.class), "temp");
	    JSwitch sw = stepMethod.body()._switch(stateVar.invoke("getCurrent"));
	    // Now to go through the transitions. They need to be sorted by priority for this.
	    Collections.sort(transitions);
	    
	    // We're going in order of priority, but the final code will be ordered by the
	    // starting state. This map will let us grab the correct code block to add on to.
	    // We also need the last if statement for each state.
	    Map<State, JCase> caseMap = new HashMap<State, JCase>();
	    Map<State, JConditional> lastCondition = new HashMap<State, JConditional>();
	    
	    for (Transition t : transitions) {
	        // Do we have this starting state?
	        if ( ! caseMap.containsKey(t.start)) {
	            // Make it
	            caseMap.put(t.start, sw._case(JExpr.lit(indexOf(t.start))));
	        }
	        // Let the transition do its thing
	        JConditional cond = t.addTransition(caseMap.get(t.start).body(), cm, stateVar, lastCondition.get(t.start));
	        // This is now the latest if statement for this state
	        lastCondition.put(t.start, cond);
	    }
	    // All done! We can add break statements to each of them
	    for (State state : caseMap.keySet()) {
	        caseMap.get(state).body()._break();
	    }
	    // Finally, after the case/switch we need to execute the in actions
	    // of whatever state we're (now) in.
	    stepMethod.body().directStatement("/* In Actions executed here: */");
	    JSwitch inActionSwitch = stepMethod.body()._switch(stateVar.invoke("getNext"));
	    for (State state : states) {
	        // Skip states with no In actions
	        if (state.inActions.size() == 0) continue;
	        
	        JCase theCase = inActionSwitch._case(JExpr.lit(indexOf(state)));
	        for (PlexilAction a : state.inActions) {
	            a.addActionToBlock(theCase.body(), cm);
	        }
	        theCase.body()._break();
	    }
	    
	    
	    // That's all for the step function. Now we need to handle mapping back
	    // to Plexil states for the nodes that we're in charge of.
	    // The function is going to need to map the state integer into a NodeState.
	    for (NodeUID nodeId : nodeIds) {
	        // Make sure we put it where NodeStateReferences will be looking:
	        JMethod stateMethod = clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), 
	                NodeStateReference.nameOfStateMethodForNode(nodeId));
	        
	        // Map from Plexil state to all States tagged with it.
	        Map<NodeState, List<Integer>> reverseMapping = new HashMap<NodeState, List<Integer>>();
	        
	        for (State s : states) {
	            NodeState ns = s.tags.get(nodeId);
	            if (ns == null) throw new RuntimeException("No tag for "+nodeId+" in state "+s);
	            
	            if ( ! reverseMapping.containsKey(ns)) {
	                reverseMapping.put(ns, new ArrayList<Integer>());
	            }
	            reverseMapping.get(ns).add(indexOf(s));
	        }
	        
	        // Big switch statement on all States. The tag for this nodeId tells
            // us what to return. 
            sw = stateMethod.body()._switch(stateVar.invoke("getCurrent"));
	        for (NodeState ns : reverseMapping.keySet()) {
	            List<Integer> ints = reverseMapping.get(ns);
	            JCase lastCase = null;
	            for (Integer stateInt : ints) {
	                lastCase = sw._case(JExpr.lit(stateInt));
	            }
	            lastCase.body()._return(cm.ref(NodeState.class).staticRef(ns.toString()));
	        }
	        stateMethod.body()._throw(JExpr._new(cm.ref(RuntimeException.class))
	                .arg(JExpr.lit(
	                        "No state mapping found for "+nodeId)));
	    }
	}
	
	
}
