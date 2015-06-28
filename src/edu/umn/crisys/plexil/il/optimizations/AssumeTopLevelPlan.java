package edu.umn.crisys.plexil.il.optimizations;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.RootAncestorEndExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExitExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.expr.il.RootParentStateExpr;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class AssumeTopLevelPlan extends ILExprModifier<Void> {
	
	public static boolean looksLikeTopLevelPlan(PlexilPlan plan) {
		// Libraries usually have interfaces, so if there isn't one, it's
		// probably a top level plan. 
		return ! plan.getRootNode().getInterface().isDefined();
	}
	
	private AssumeTopLevelPlan() {}

	/**
	 * In Plexil semantics, a plan that is not being used as a Library node
	 * operates as if its parent is an Executing node where nothing goes wrong.
	 * If the plan will never be used as a library, you can replace the 
	 * root node's parent checks with constants, which is exactly what this
	 * optimization does. 
	 * 
	 * @param ilPlan
	 */
	public static void optimize(Plan ilPlan) {
		ilPlan.modifyAllExpressions(new AssumeTopLevelPlan(), null);
		ilPlan.setTopLevelPlan(true);
	}

	@Override
	public Expression visit(RootParentStateExpr state,
			Void param) {
		// Plexil root states are always EXECUTING.
		return NodeState.EXECUTING;
	}

	@Override
	public Expression visit(RootAncestorExitExpr ancExit,
			Void param) {
		// Their parent isn't exiting.
		return BooleanValue.get(false);
	}

	@Override
	public Expression visit(RootAncestorEndExpr ancEnd,
			Void param) {
		// Their parent isn't ending.
		return BooleanValue.get(false);
	}

	@Override
	public Expression visit(
			RootAncestorInvariantExpr ancInv, Void param) {
		// Parent's invariant hasn't failed.
		return BooleanValue.get(true);
	}

}
