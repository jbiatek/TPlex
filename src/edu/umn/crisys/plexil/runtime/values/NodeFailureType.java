package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILType;

public enum NodeFailureType implements PValue {
	UNKNOWN,
	PRE_CONDITION_FAILED,
	POST_CONDITION_FAILED,
	INVARIANT_CONDITION_FAILED,
	PARENT_FAILED,
	EXITED,
	PARENT_EXITED;

	@Override
	public boolean isKnown() {
		return this != UNKNOWN;
	}

	@Override
	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public ILType getType() {
		return ILType.FAILURE;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}
	@Override
	public String asString() {
		return toString();
	}

}
