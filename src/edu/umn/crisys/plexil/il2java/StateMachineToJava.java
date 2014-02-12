package edu.umn.crisys.plexil.il2java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.codemodel.JBlock;
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
import edu.umn.crisys.plexil.translator.il.NodeStateMachine;
import edu.umn.crisys.plexil.translator.il.NodeUID;
import edu.umn.crisys.plexil.translator.il.State;
import edu.umn.crisys.plexil.translator.il.Transition;
import edu.umn.crisys.plexil.translator.il.action.PlexilAction;

public class StateMachineToJava {

	private StateMachineToJava() {}
	
	public static void callStepFunction(NodeStateMachine nsm, JBlock block) {
		block.invoke(getStepMethodName(nsm));
	}
	
	public static String getMappingMethodName(NodeUID uid) {
		return "STATE___"+uid.toCleanString();
	}

	public static void addStateMachineToClass(NodeStateMachine nsm, JDefinedClass clazz) {
		JCodeModel cm = clazz.owner();
	
		// Create the variable to hold the current state
		JClass stateClass = cm.ref(SimpleCurrentNext.class).narrow(cm.ref(Integer.class));
		JFieldVar stateVar = clazz.field(JMod.PRIVATE, stateClass, NameUtils.clean(nsm.nsmId+".state"), 
				JExpr._new(stateClass).arg(JExpr.lit(0)));
	
		// Now we make the step function.
		addStepFunction(nsm, stateVar, clazz);
		// And also the mapping function back to Plexil state.
		addMappingFunctions(nsm, stateVar, clazz);
	
	}

	private static String getStepMethodName(NodeStateMachine nsm) {
	    return NameUtils.clean("MicroStep___"+nsm.nsmId);
	}

	private static void addStepFunction(NodeStateMachine nsm, JFieldVar stateVar, JDefinedClass clazz) {
		JCodeModel cm = clazz.owner();

		JMethod stepMethod = clazz.method(JMod.NONE, cm.VOID, getStepMethodName(nsm));
		
		JSwitch sw = stepMethod.body()._switch(stateVar.invoke("getCurrent"));
		// Now to go through the transitions. They need to be sorted by priority for this.
		Collections.sort(nsm.transitions);

		// We're going in order of priority, but the final code will be ordered by the
		// starting state. This map will let us grab the correct code block to add on to.
		// We also need the last if statement for each state.
		Map<State, JCase> caseMap = new HashMap<State, JCase>();
		Map<State, JConditional> lastCondition = new HashMap<State, JConditional>();

		for (Transition t : nsm.transitions) {
			// Do we have this starting state?
			if ( ! caseMap.containsKey(t.start)) {
				// Make it
				caseMap.put(t.start, sw._case(JExpr.lit(nsm.indexOf(t.start))));
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
		for (State state : nsm.states) {
			// Skip states with no In actions
			if (state.inActions.size() == 0) continue;

			JCase theCase = inActionSwitch._case(JExpr.lit(nsm.indexOf(state)));
			for (PlexilAction a : state.inActions) {
				a.addActionToBlock(theCase.body(), cm);
			}
			theCase.body()._break();
		}

	}
	
	private static void addMappingFunctions(NodeStateMachine nsm, JFieldVar stateVar, JDefinedClass clazz) {
		JCodeModel cm = clazz.owner();
		
		// Now we need to handle mapping back to Plexil states for the nodes 
		// that we're in charge of.
		// Each function is going to need to map the state integer into a NodeState.
		for (NodeUID nodeId : nsm.getNodeIds()) {
			// Make sure we put it where NodeStateReferences will be looking:
			JMethod stateMethod = clazz.method(JMod.PUBLIC, cm.ref(NodeState.class), getMappingMethodName(nodeId));

			// Map from Plexil state to all States tagged with it.
			Map<NodeState, List<Integer>> reverseMapping = new HashMap<NodeState, List<Integer>>();

			for (State s : nsm.states) {
				NodeState ns = s.tags.get(nodeId);
				if (ns == null) throw new RuntimeException("No tag for "+nodeId+" in state "+s);

				if ( ! reverseMapping.containsKey(ns)) {
					reverseMapping.put(ns, new ArrayList<Integer>());
				}
				reverseMapping.get(ns).add(nsm.indexOf(s));
			}

			// Big switch statement on all States. The tag for this nodeId tells
			// us what to return. 
			JSwitch sw = stateMethod.body()._switch(stateVar.invoke("getCurrent"));
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
