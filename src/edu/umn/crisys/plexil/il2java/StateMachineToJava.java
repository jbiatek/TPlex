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
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JSwitch;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.action.PlexilAction;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard.Condition;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;

public class StateMachineToJava {

    public static boolean BIASING = true;
	
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
		Map<State, JMethod> methodMap = new HashMap<State, JMethod>();
		Map<State, JConditional> lastCondition = new HashMap<State, JConditional>();

		for (Transition t : nsm.transitions) {
			// Do we have this starting state?
			if ( ! methodMap.containsKey(t.start)) {
				// Make it
				JCase mainCase = sw._case(JExpr.lit(nsm.indexOf(t.start)));
				methodMap.put(t.start, clazz.method(JMod.PRIVATE, cm.VOID, getStepMethodName(nsm)+"__"+t.start.getIndex()));
				// Need to declare a temp variable inside each method. 
				methodMap.get(t.start).body().decl(cm.ref(PBoolean.class), "temp");
				
				// The case just needs to invoke the new method.
				mainCase.body().invoke(methodMap.get(t.start));
				mainCase.body()._break();
			}
			// Let the transition do its thing
			JConditional cond = addTransition(t, nsm.thePlan,
					methodMap.get(t.start).body(), cm, stateVar, lastCondition.get(t.start));
			// This is now the latest if statement for this state
			lastCondition.put(t.start, cond);
		}
		// Finally, after the case/switch we need to execute the in actions
		// of whatever state we're (now) in.
		stepMethod.body().directStatement("/* In Actions executed here: */");
		JSwitch inActionSwitch = stepMethod.body()._switch(stateVar.invoke("getNext"));
		for (State state : nsm.states) {
			// Skip states with no In actions
			if (state.inActions.size() == 0) continue;

			JCase theCase = inActionSwitch._case(JExpr.lit(nsm.indexOf(state)));
			ActionToJava a2j = new ActionToJava(cm, nsm.thePlan);
			for (PlexilAction a : state.inActions) {
				a.accept(a2j, theCase.body());
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
	
	
	
	/**
     * Add this transition on to the previous one. If this is the first, 
     * pass in null.
     * 
     * @param block The block of code we're adding to
     * @param cm
     * @param prev The conditional returned by the last transition
     * @return The JConditional for this transition. Pass it to the next Transition.
     */
	public static JConditional addTransition(Transition t, Plan ilPlan, 
			JBlock block, JCodeModel cm, JFieldVar stateVar, JConditional prev) {
		// Is this our start state?
//	    JExpression condExp = JExpr.invoke("getState").eq(
//				cm.ref(NodeState.class).staticRef(start.toString()));
		
	    
	    
		// Is each of our guards satisfied?
	    JExpression condExp = null;
		for (TransitionGuard guard : t.guards) {
		    if (guard.isAlwaysActive()) {
		        // We can skip this, it's just going to be true
		        continue;
		    }
		    if (condExp != null) {
		        condExp = condExp.cand(wrap(guard, cm));
		    } else {
		        condExp = wrap(guard, cm);
		    }
		}
		
		// Figure out if we're doing if, else if, nothing at all...
		JConditional current;
		JBlock thenBlock;
		if (condExp == null ) {
		    // There are no guards, so we don't need an if statement.
		    if (prev == null) {
		        // No previous if statements either. We're the first and last
		        // transition. Let's just put it in directly then!
		        current = null;
		        thenBlock = block;
		    } else {
		        // There's a previous if statement. We'll add ourselves on
		        // as an else.
		        current = null;
		        thenBlock = prev._else();
		    }
		    
		} else if (prev == null) {
		    // We have guards, and we're first in line. We'll make an if:
			current = block._if(condExp);
			thenBlock = current._then().block();
		} else {
		    // We have guards, but we're not first. We'll daisy chain:
			current = prev._elseif(condExp);
			thenBlock = current._then().block();
		}
		
		// Now for the action, which goes into thenBlock.
		// Add a readable comment:
		thenBlock.directStatement("/*\n"+t.toString()+"\n*/");
		// Some debug code to print what's happening if desired
		thenBlock._if(cm.ref(JavaPlan.class).staticRef("DEBUG"))
			._then()
				.invoke(cm.ref(System.class).staticRef("out"), "println")
					.arg(JExpr.lit(t.description));
		
		// Perform the actions, if any, then move to the destination state.
		ActionToJava a2j = new ActionToJava(cm, ilPlan);
		for (PlexilAction action : t.actions) {
			action.accept(a2j, thenBlock);
		}

		for (PlexilAction action : t.end.entryActions) {
			action.accept(a2j, thenBlock);
		}

		thenBlock.invoke(stateVar, "setNext").arg(JExpr.lit(t.end.getIndex()));
		thenBlock.invoke("commitAfterMicroStep").arg(stateVar);
		thenBlock.invoke("changeOccurred");
		
		// Return our if statement to be daisy chained off of
		return current;
	}
	
	

    /**
     * Create a Java boolean expression indicating whether this Plexil
     * expression has this Condition.
     * @param expr
     * @return
     */
    private static JExpression wrap(TransitionGuard guard, JCodeModel cm) {
    	ILExpression expr = guard.getExpression();
    	Condition cond = guard.getCondition();
        if (BIASING) {
            switch (cond) {
            case TRUE:
                return ILExprToJava.toJavaBiased(expr, cm, true);
            case FALSE:
                return ILExprToJava.toJavaBiased(expr, cm, false);
            case UNKNOWN:
                return ILExprToJava.toJava(expr, cm).invoke("isUnknown");
            case NOTTRUE:
                return ILExprToJava.toJavaBiased(expr, cm, true).not();
            case NOTFALSE:
                return ILExprToJava.toJavaBiased(expr, cm, false).not();
            case KNOWN:
                return ILExprToJava.toJava(expr, cm).invoke("isKnown");
            }
            throw new RuntimeException("Add this case to wrap(): "+cond);
        } else {
            switch (cond) {
            case TRUE:
                return ILExprToJava.toJava(expr, cm).invoke("isTrue");
            case FALSE:
                return ILExprToJava.toJava(expr, cm).invoke("isFalse");
            case UNKNOWN:
                return ILExprToJava.toJava(expr, cm).invoke("isUnknown");
            case NOTTRUE:
                return ILExprToJava.toJava(expr, cm).invoke("isNotTrue");
            case NOTFALSE:
                return ILExprToJava.toJava(expr, cm).invoke("isNotFalse");
            case KNOWN:
                return ILExprToJava.toJava(expr, cm).invoke("isKnown");
            }
            throw new RuntimeException("Add this case to wrap(): "+cond);

        }

    }
}


