package edu.umn.crisys.plexil.script.ast;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;

public class CommandAck implements Event {
	private FunctionCall call;
	private CommandHandleState result;
	
	public CommandAck(FunctionCall call, CommandHandleState result) {
		this.call = call;
		this.result = result;
	}
	
	public FunctionCall getCall() {
		return call;
	}
	
	public CommandHandleState getResult() {
		return result;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof CommandAck)) {
			return false;
		}
		CommandAck o = (CommandAck) other;
		return o.call.equals(this.call) && o.result.equals(this.result);
	}
	
	@Override
	public int hashCode() {
		return call.hashCode() + result.hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitCommandAck(this, param);
	}

}