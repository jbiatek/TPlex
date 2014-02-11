package edu.umn.crisys.plexil.java.values;


public enum NodeFailureType implements PValue {
	PRE_CONDITION_FAILED,
	POST_CONDITION_FAILED,
	INVARIANT_CONDITION_FAILED,
	PARENT_FAILED,
	EXITED,
	PARENT_EXITED,
	UNKNOWN;

	public PBoolean equalTo(PValue other) {
		return PValue.Util.enumEqualTo(this, other);
	}
	
	@Override
	public boolean isKnown() {
		return !this.equals(UNKNOWN);
	}

	@Override
	public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.FAILURE;
	}

	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}

}
