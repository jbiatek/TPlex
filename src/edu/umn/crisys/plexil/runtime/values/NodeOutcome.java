package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILType;

public enum NodeOutcome implements PValue {
	UNKNOWN,
	SUCCESS,
	FAILURE,
	SKIPPED,
	INTERRUPTED;

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
		return ILType.OUTCOME;
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
