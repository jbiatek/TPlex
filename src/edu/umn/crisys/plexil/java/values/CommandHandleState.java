package edu.umn.crisys.plexil.java.values;


public enum CommandHandleState implements PValue {
	COMMAND_ACCEPTED,
	COMMAND_SUCCESS,
	COMMAND_RCVD_BY_SYSTEM,
	COMMAND_SENT_TO_SYSTEM,
	COMMAND_FAILED,
	COMMAND_DENIED,
	COMMAND_ABORTED,
	COMMAND_ABORT_FAILED,
	UNKNOWN;
	
	public boolean isKnown() {
		return !this.equals(UNKNOWN);
	}

	@Override
	public boolean isUnknown() {
		return this.equals(UNKNOWN);
	}

	@Override
	public PBoolean equalTo(PValue o) {
		return PValue.Util.enumEqualTo(this, o);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.COMMAND_HANDLE;
	}

	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}
}
