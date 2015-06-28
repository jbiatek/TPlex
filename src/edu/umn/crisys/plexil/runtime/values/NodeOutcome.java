package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.PlexilType;

public enum NodeOutcome implements PValue {
	SUCCESS,
	FAILURE,
	SKIPPED,
	INTERRUPTED,
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
		return PlexilType.OUTCOME;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeOutcome(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}

}
