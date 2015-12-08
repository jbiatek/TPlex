package edu.umn.crisys.plexil.il.optimizations;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NodeState;

public class AssumeTopLevelPlan extends ILExprModifier<Void> {
	
	public static boolean looksLikeTopLevelPlan(PlexilPlan plan) {
		// Libraries usually have interfaces, so if there isn't one, it's
		// probably a top level plan. 
		return ! plan.getRootNode().getInterface().isDefined();
	}
	
	public static boolean looksLikeTopLevelPlan(Plan plan) {
		return ! plan.getRootPlexilInterface().isDefined();
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
	public Expression visit(RootAncestorExpr root, Void param) {
		switch (root) {
		case STATE:
			// Plexil root states are always EXECUTING.
			return NodeState.EXECUTING;
		case EXIT:
			// Their parent isn't exiting.
			return BooleanValue.get(false);
		case END:
			// Their parent isn't ending.
			return BooleanValue.get(false);
		case INVARIANT:
			// Parent's invariant hasn't failed.
			return BooleanValue.get(true);
		default:
			throw new RuntimeException("Missing case");
		}
	}

}
