package edu.umn.crisys.plexil.runtime.values;

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
	public <P, R> R accept(PValueVisitor<P, R> visitor, P param) {
		return visitor.visitNodeFailure(this, param);
	}
	@Override
	public String asString() {
		return toString();
	}

}
