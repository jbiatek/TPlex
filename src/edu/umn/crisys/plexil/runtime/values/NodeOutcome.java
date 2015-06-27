package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;

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
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeOutcome(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}

}
