package edu.umn.crisys.plexil.il.optimizations;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
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
	public Expression visitRootParentState(RootParentStateExpr state,
			Void param) {
		// Plexil root states are always EXECUTING.
		return NodeState.EXECUTING;
	}

	@Override
	public Expression visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		// Their parent isn't exiting.
		return BooleanValue.get(false);
	}

	@Override
	public Expression visitRootParentEnd(RootAncestorEndExpr ancEnd,
			Void param) {
		// Their parent isn't ending.
		return BooleanValue.get(false);
	}

	@Override
	public Expression visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		// Parent's invariant hasn't failed.
		return BooleanValue.get(true);
	}

}
