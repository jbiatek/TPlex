package edu.umn.crisys.plexil.il2lustre;

import java.util.Set;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.EnumType;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.NamedType;
import jkind.lustre.NodeCallExpr;
import jkind.lustre.Program;
import jkind.lustre.Type;
import jkind.lustre.TypeDef;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class PlanToLustre {
	
	public static Program toLustre(Plan p, PlexilPlan originalAst) {
		ProgramBuilder pb = getProgramWithPlexilTypes();
		NodeBuilder nb = new NodeBuilder(NameUtils.clean(p.getPlanName()));
		
		
		// Add in declared lookups
		for (LookupDecl lookup : originalAst.getStateDeclarations()) {
			String rawInputId = ILExprToLustre.getRawLookupId(lookup.getName());
			String lookupId = ILExprToLustre.getLookupId(lookup.getName());
			Type type = getLustreType(lookup.getReturnValue().get().getType());
			nb.addInput(new VarDecl(rawInputId, type));
			// The "real" lookup can only change between macro steps
			nb.addLocal(new VarDecl(lookupId, type));
			// if (macro step ended) then raw input else stay the same
			nb.addEquation(new Equation(new IdExpr(lookupId), 
					new IfThenElseExpr(
							new IdExpr(ActionsToLustre.MACRO_STEP_ENDED_ID), 
							new IdExpr(rawInputId), 
							new UnaryExpr(UnaryOp.PRE, new IdExpr(lookupId)))
					));
		}
		
		for (NodeStateMachine nsm : p.getMachines()) {
			addStateMachine(nsm, nb);
		}
		
		for (ILVariable v : p.getVariables()) {
			addVariable(v, p, nb);
			if (v.getName().contains("previous_value")) {
				System.err.println(v);
			}
		}
		
		ActionsToLustre a2l = new ActionsToLustre();
		a2l.navigate(p);
		a2l.toLustre(nb);
		
		pb.addNode(nb.build());
		return pb.build();
	}
	
	private static ProgramBuilder getProgramWithPlexilTypes() {
		ProgramBuilder pb = new ProgramBuilder();
		addEnumType(pb, ILExprToLustre.PBOOLEAN);
		addEnumType(pb, ILExprToLustre.PSTATE);
		addEnumType(pb, ILExprToLustre.PCOMMAND);
		addEnumType(pb, ILExprToLustre.POUTCOME);
		addEnumType(pb, ILExprToLustre.PFAILURE);
		
		// Some Plexil operators too
		NodeBuilder p_and = new NodeBuilder("p_and");
		p_and.addInput(new VarDecl("first", ILExprToLustre.PBOOLEAN));
		p_and.addInput(new VarDecl("second", ILExprToLustre.PBOOLEAN));
		p_and.addOutput(new VarDecl("result", ILExprToLustre.PBOOLEAN));
		p_and.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_false or second = p_false) then p_false\n"
           +"else if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else p_true")));
		pb.addNode(p_and.build());
		
		NodeBuilder p_or = new NodeBuilder("p_or");
		p_or.addInput(new VarDecl("first", ILExprToLustre.PBOOLEAN));
		p_or.addInput(new VarDecl("second", ILExprToLustre.PBOOLEAN));
		p_or.addOutput(new VarDecl("result", ILExprToLustre.PBOOLEAN));
		p_or.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_true or second = p_true) then p_true\n"
           +"else if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else p_false")));
		pb.addNode(p_or.build());
		
		

		NodeBuilder p_not = new NodeBuilder("p_not");
		p_not.addInput(new VarDecl("value", ILExprToLustre.PBOOLEAN));
		p_not.addOutput(new VarDecl("result", ILExprToLustre.PBOOLEAN));
		p_not.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if value = p_true then p_false\n"
           +"else if value = p_false then p_true\n"
           +"else p_unknown")));
		pb.addNode(p_not.build());
		
		NodeBuilder p_eq_bool = new NodeBuilder("p_eq_boolean");
		p_eq_bool.addInput(new VarDecl("first", ILExprToLustre.PBOOLEAN));
		p_eq_bool.addInput(new VarDecl("second", ILExprToLustre.PBOOLEAN));
		p_eq_bool.addOutput(new VarDecl("result", ILExprToLustre.PBOOLEAN));
		p_eq_bool.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else if (first = second) then p_true\n"
           +"else p_false")));
		pb.addNode(p_eq_bool.build());
		

		NodeBuilder to_pboolean = new NodeBuilder("to_pboolean");
		to_pboolean.addInput(new VarDecl("value", NamedType.BOOL));
		to_pboolean.addOutput(new VarDecl("result", ILExprToLustre.PBOOLEAN));
		to_pboolean.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if value then p_true else p_false")));
		pb.addNode(to_pboolean.build());
		
		return pb;
	}
	
	private static void addEnumType(ProgramBuilder pb, EnumType et) {
		pb.addType(new TypeDef(et.id, et));
	}
	private static Type getLustreType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return ILExprToLustre.PBOOLEAN;
		case INTEGER:
			return NamedType.INT;
		case REAL:
			return NamedType.REAL;
		case STATE:
			return ILExprToLustre.PSTATE;
		case COMMAND_HANDLE:
			return ILExprToLustre.PCOMMAND;
		case OUTCOME:
			return ILExprToLustre.POUTCOME;
		case FAILURE:
			return ILExprToLustre.PFAILURE;
		case ARRAY:
		case BOOLEAN_ARRAY:
		case INTEGER_ARRAY:
		case REAL_ARRAY:
			throw new RuntimeException("Arrays not supported yet");
		case STRING:
		case STRING_ARRAY:
			throw new RuntimeException("Strings not supported yet");
		case UNKNOWN:
		default:
			throw new RuntimeException("Lustre needs everything to be typed!");
		}
	}
	
	private static void addVariable(ILVariable v, Plan p, NodeBuilder nb) {
		// Assignments are handled by actions. We'll do the declaration 
		// and leave the rest to them. 
		nb.addLocal(new VarDecl(ILExprToLustre.getVariableId(v), getLustreType(v.getType())));
	}

	private static String getStateId(NodeStateMachine nsm) {
		return NameUtils.clean(nsm.getStateMachineId());
	}
	
	private static IdExpr getStateExpr(NodeStateMachine nsm) {
		return new IdExpr(getStateId(nsm));
	}
	
	private static String getStateMapperId(NodeUID uid) {
		return NameUtils.clean(uid.toString()+"__state");
	}
	
	public static Expr getPlexilStateExprForNode(NodeUID uid) {
		return new UnaryExpr(UnaryOp.PRE, new IdExpr(getStateMapperId(uid)));
	}

	private static void addStateMachine(NodeStateMachine nsm, NodeBuilder nb) {
		String id = getStateId(nsm);
		
		VarDecl stateVar = new VarDecl(id, NamedType.INT);
		
		nb.addLocal(stateVar);
		nb.addEquation(new Equation(getStateExpr(nsm), stateMachineExpr(nsm)));
		
		// We also need to map back to Plexil state.
		for (NodeUID uid : nsm.getNodeIds()) {
			VarDecl mapper = new VarDecl(getStateMapperId(uid), ILExprToLustre.PSTATE);
			Expr map = new IdExpr("inactive");
			for (State s : nsm.getStates()) {
				map = new IfThenElseExpr(
						// if the real state variable is this one
						new BinaryExpr(new IdExpr(getStateId(nsm)), BinaryOp.EQUAL, 
								new IdExpr(s.getIndex()+"")),
						// then return the tag
						ILExprToLustre.toLustre(s.tags.get(uid), PlexilType.STATE),
						// else, the rest
						map);
			}
			
			nb.addLocal(mapper);
			nb.addEquation(new Equation(new IdExpr(getStateMapperId(uid)), map));
		}
	}
	
	private static Expr stateMachineExpr(NodeStateMachine nsm) {
		// If no transitions are active, don't change states.
		Expr bigIf = new UnaryExpr(UnaryOp.PRE, getStateExpr(nsm));
		
		// Move backward and build up the big if statement
		for (int i = nsm.getTransitions().size()-1; i >= 0; i--) {
			Transition t = nsm.getTransitions().get(i);
			
			// If guard is satisfied, then pick this end state, else
			// (the rest of the transitions)
			bigIf = new IfThenElseExpr(createGuardWithStateCheck(getStateExpr(nsm), t), 
					new IdExpr(t.end.getIndex()+""), 
					bigIf);
		}
		
		// We have the big if, but we need to initialize. By IL semantics, 
		// we always start in state 0. 
		return new BinaryExpr(new IdExpr("0"), BinaryOp.ARROW, bigIf);
	}
	
	/**
	 * Get the guard expression for this transition. It doesn't include the 
	 * precondition that the current state is this transition's start state,
	 * and it also doesn't include the precondition that there isn't a higher
	 * priority transition active.  
	 * @param t
	 * @return
	 */
	public static Expr getGuardExprFor(Transition t) {
		Expr guardExpr = null;
		for (TransitionGuard guard : t.guards) {
			Expr thisGuardExpr = ILExprToLustre.toLustre(guard.getExpression(), PlexilType.BOOLEAN);
			BinaryOp op;
			Expr compareTo;
			switch (guard.getCondition()) {
			case TRUE:
				op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_TRUE;
				break;
			case FALSE:
				op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_FALSE;
				break;
			case UNKNOWN:
				op = BinaryOp.EQUAL; compareTo = ILExprToLustre.P_UNKNOWN;
				break;
			case NOTTRUE:
				op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_TRUE;
				break;
			case NOTFALSE:
				op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_FALSE;
				break;
			case KNOWN:
				op = BinaryOp.NOTEQUAL; compareTo = ILExprToLustre.P_UNKNOWN;
				break;
			default:
				throw new RuntimeException("Unknown case!");	
			}
			// Add in the proper comparison
			thisGuardExpr = new BinaryExpr(thisGuardExpr, op, compareTo);
			// Whatever we had before, and also this one
			if (guardExpr == null) {
				guardExpr = thisGuardExpr;
			} else {
				guardExpr = new BinaryExpr(guardExpr, BinaryOp.AND, thisGuardExpr);
			}
		}
		
		if (guardExpr == null) {
			// No guards, so return true I guess!
			return new IdExpr("true");
		}
		
		return guardExpr;

	}
	
	/**
	 * Creates an expression that is true if and only if the given transition is
	 * active in this step. 
	 * @param t
	 * @param nsm
	 * @param stateId
	 * @return
	 */
	public static Expr getGuardForThisSpecificTransition(Transition t, NodeStateMachine nsm) {
		// This transition has to be active, but also all the transitions before 
		// it have to be inactive. 
		Expr specificGuard = createGuardWithStateCheck(getStateExpr(nsm), t);
		
		Set<Transition> highers = nsm.getHigherTransitionsThan(t);
		
		for (Transition higherPriority : highers) {
			specificGuard = new BinaryExpr(specificGuard, BinaryOp.AND, 
					new UnaryExpr(UnaryOp.NOT, getGuardExprFor(higherPriority)));
		}
		
		return specificGuard;
	}
	
	public static Expr getEnteringThisSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState = getStateExpr(nsm);
		Expr previousState = new UnaryExpr(UnaryOp.PRE, currentState);
		Expr specifiedState = new IdExpr(s.getIndex()+"");
		return new BinaryExpr(
				new BinaryExpr(previousState, BinaryOp.NOTEQUAL, specifiedState), 
				BinaryOp.AND, 
				new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState));
	}
	
	public static Expr getCurrentlyInSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState = getStateExpr(nsm);
		Expr specifiedState = new IdExpr(s.getIndex()+"");
		return new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState);
	}
	
	public static Expr getStateIsUnchanged(NodeStateMachine nsm) {
		return new BinaryExpr(getStateExpr(nsm), 
				BinaryOp.EQUAL, 
				new UnaryExpr(UnaryOp.PRE, getStateExpr(nsm)));
	}
	
	/**
	 * Create a guard that includes a check that the current state is this
	 * transition's origin state. Does NOT include a check for a higher priority
	 * transition. It's assumed that you're checking transitions in order.
	 * @param stateId
	 * @param t
	 * @return
	 */
	public static Expr createGuardWithStateCheck(Expr stateId, Transition t) {
		// First off, we need to have been in the start state.
		Expr guardExpr = new BinaryExpr(
				new UnaryExpr(UnaryOp.PRE, stateId), 
				BinaryOp.EQUAL, 
				new IdExpr(t.start.getIndex()+""));
		
		// Then the transition's guards have to apply, if any.
		if (t.guards.size() == 0) {
			// Oh, it's always taken. Well we're done then. 
			return guardExpr;
		}
		return new BinaryExpr(guardExpr, BinaryOp.AND, getGuardExprFor(t));

	}

}
