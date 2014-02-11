package edu.umn.crisys.plexil.java.values;


public enum NodeOutcome implements PValue {
	SUCCESS,
	FAILURE,
	SKIPPED,
	INTERRUPTED,
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
		return PlexilType.OUTCOME;
	}

	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}
	
}
