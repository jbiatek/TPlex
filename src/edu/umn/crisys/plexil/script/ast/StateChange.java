package edu.umn.crisys.plexil.script.ast;

import edu.umn.crisys.plexil.java.values.PValue;

public class StateChange implements Event {
	private FunctionCall call;
	private PValue value;
	
	public StateChange(FunctionCall call, PValue value) {
		this.call = call;
		this.value = value;
	}

	public FunctionCall getLookup() {
		return call;
	}
	
	public PValue getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof StateChange)) {
			return false;
		}
		StateChange o = (StateChange) other;
		return o.call.equals(this.call) && o.value.equals(this.value);
	}
	
	@Override
	public int hashCode() {
		return call.hashCode() + value.hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitStateChange(this, param);
	}
}