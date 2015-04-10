package edu.umn.crisys.plexil.il2lustre;

import java.util.Set;

import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.BoolExpr;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.NamedType;
import jkind.lustre.Program;
import jkind.lustre.Type;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class PlanToLustre {
	
	public static Program toLustre(Plan p, PlexilPlan originalAst) {
		ProgramBuilder pb = new ProgramBuilder();
		NodeBuilder nb = new NodeBuilder(NameUtils.clean(p.getPlanName()));
		
		for (LookupDecl lookup : originalAst.getStateDeclarations()) {
			nb.addInput(new VarDecl(
					ILExprToLustre.getLookupId(lookup.getName()),
					getLustreType(lookup.getReturnValue().getType())));
					
		}
		
		for (NodeStateMachine nsm : p.getMachines()) {
			addStateMachine(nsm, nb);
		}
		
		for (ILVariable v : p.getVariables()) {
			addVariable(v, p, nb);
		}
		
		ActionsToLustre a2l = new ActionsToLustre();
		a2l.navigate(p);
		a2l.toLustre(nb);
		
		pb.addNode(nb.build());
		return pb.build();
	}
	
	private static Type getLustreType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return ILExprToLustre.PBOOLEAN;
		case INTEGER:
			return NamedType.INT;
		case REAL:
			return NamedType.BOOL;
		case STATE:
			return NamedType.INT;
		default: 
			return new NamedType(t.toString().toLowerCase());
		}
	}
	
	private static void addVariable(ILVariable v, Plan p, NodeBuilder nb) {
		nb.addLocal(new VarDecl(ILExprToLustre.getVarName(v), getLustreType(v.getType())));
		// Assignments are handled by actions. 
	}


	private static void addStateMachine(NodeStateMachine nsm, NodeBuilder nb) {
		String id = NameUtils.clean(nsm.getStateMachineId());
		
		VarDecl stateVar = new VarDecl(id, NamedType.INT);
		
		nb.addLocal(stateVar);
		IdExpr idExpr = new IdExpr(id);
		nb.addEquation(new Equation(idExpr, stateMachineExpr(idExpr, nsm)));
	}
	
	private static Expr stateMachineExpr(Expr stateId, NodeStateMachine nsm) {
		// If no transitions are active, don't change states.
		Expr bigIf = new UnaryExpr(UnaryOp.PRE, stateId);
		
		// Move backward and build up the big if statement
		for (int i = nsm.getTransitions().size()-1; i >= 0; i--) {
			Transition t = nsm.getTransitions().get(i);
			
			// If guard is satisfied, then pick this end state, else
			// (the rest of the transitions)
			bigIf = new IfThenElseExpr(createCompleteGuard(stateId, t), 
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
			// Whatever we had before, and also this one
			if (guardExpr == null) {
				guardExpr = thisGuardExpr;
			} else {
				guardExpr = new BinaryExpr(guardExpr, BinaryOp.AND,
						new BinaryExpr(thisGuardExpr, op, compareTo));
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
	public static Expr getGuardForThisSpecificTransition(Transition t, NodeStateMachine nsm, Expr stateId) {
		// This transition has to be active, but also all the transitions before 
		// it have to be inactive. 
		Expr specificGuard = createCompleteGuard(stateId, t);
		
		Set<Transition> highers = nsm.getHigherTransitionsThan(t);
		
		for (Transition higherPriority : highers) {
			specificGuard = new BinaryExpr(specificGuard, BinaryOp.AND, 
					new UnaryExpr(UnaryOp.NOT, getGuardExprFor(higherPriority)));
		}
		
		return null;
	}
	
	/**
	 * Create a guard that includes a check that the current state is this
	 * transition's origin state. Does NOT include a check for a higher priority
	 * transition. It's assumed that you're checking transitions in order.
	 * @param stateId
	 * @param t
	 * @return
	 */
	public static Expr createCompleteGuard(Expr stateId, Transition t) {
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
