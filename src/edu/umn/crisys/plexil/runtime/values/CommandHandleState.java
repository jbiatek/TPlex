package edu.umn.crisys.plexil.runtime.values;

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
		return this != UNKNOWN;
	}

	@Override
	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	@Override
	public PlexilType getType() {
		return PlexilType.COMMAND_HANDLE;
	}

	@Override
	public <P, R> R accept(PValueVisitor<P, R> visitor, P param) {
		return visitor.visitCommandHandleState(this, param);
	}
	
	@Override
	public String asString() {
		return toString();
	}
}
