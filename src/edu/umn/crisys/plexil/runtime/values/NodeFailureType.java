package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.PlexilType;

public enum NodeFailureType implements PValue {
	PRE_CONDITION_FAILED,
	POST_CONDITION_FAILED,
	INVARIANT_CONDITION_FAILED,
	PARENT_FAILED,
	EXITED,
	PARENT_EXITED,
	UNKNOWN;

	@Override
	public boolean isKnown() {
		return this != UNKNOWN;
	}

	@Override
	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public PlexilType getType() {
		return PlexilType.FAILURE;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeFailure(this, param);
	}
	@Override
	public String asString() {
		return toString();
	}

}
