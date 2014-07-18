package edu.umn.crisys.plexil.main.optimizations;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.statemachine.TransitionGuard;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class AssumeTopLevelPlan extends ILExprModifier<Void> {
	
	public static boolean looksLikeTopLevelPlan(PlexilPlan plan) {
		// Libraries usually have interfaces, so if there isn't one, it's
		// probably a top level plan. 
		return ! plan.getRootNode().getInterface().isDefined();
	}
	
	private AssumeTopLevelPlan() {}

	public static void optimize(Plan ilPlan) {
		AssumeTopLevelPlan visitor = new AssumeTopLevelPlan();
		
		for (NodeStateMachine nsm : ilPlan.getMachines()) {
			for (Transition t : nsm.getTransitions()) {
				for (TransitionGuard g : t.guards) {
					// Swap the guard out with whatever we say
					g.setExpression(g.getExpression().accept(visitor, null));
				}
			}
		}
	}

	@Override
	public ILExpression visitRootParentState(RootParentStateExpr state,
			Void param) {
		// Plexil root states are always EXECUTING.
		return NodeState.EXECUTING;
	}

	@Override
	public ILExpression visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		// Their parent isn't executing.
		return BooleanValue.get(false);
	}

	@Override
	public ILExpression visitRootParentEnd(RootAncestorEndExpr ancEnd,
			Void param) {
		// Their parent isn't ending.
		return BooleanValue.get(false);
	}

	@Override
	public ILExpression visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		// Parent's invariant hasn't failed.
		return BooleanValue.get(true);
	}

}
