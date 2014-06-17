package edu.umn.crisys.plexil.script.ast;

import edu.umn.crisys.plexil.java.values.PValue;

public class CommandReturn implements Event {
	private FunctionCall call;
	private PValue value;
	
	public CommandReturn(FunctionCall call, PValue value) {
		this.call = call;
		this.value = value;
	}
	
	public FunctionCall getCall() {
		return call;
	}
	
	public PValue getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof CommandReturn)) {
			return false;
		}
		CommandReturn o = (CommandReturn) other;
		return o.call.equals(this.call) && o.value.equals(this.value);
	}
	
	@Override
	public int hashCode() {
		return call.hashCode() + value.hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitCommandReturn(this, param);
	}

}