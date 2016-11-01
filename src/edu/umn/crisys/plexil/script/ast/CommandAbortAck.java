package edu.umn.crisys.plexil.script.ast;

/** 
 * Represents the environment acknowledging that the PLEXIL plan aborted
 * a command. 
 * 
 * Note that in PLEXILScript, you are required to specify a "value" 
 * for this tag. However, in practice this value is completely ignored, or
 * at least "true", "false", and "unknown" all cause the abort to be 
 * acknowledged the exact same way. 
 * @author jbiatek
 *
 */
public class CommandAbortAck implements Event {

	private FunctionCall call;
	
	public CommandAbortAck(FunctionCall call) {
		this.call = call;
	}
	
	public FunctionCall getCall() {
		return call;
	}
	
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof CommandReturn)) {
			return false;
		}
		CommandAbortAck o = (CommandAbortAck) other;
		return o.call.equals(this.call);
	}
	
	@Override
	public int hashCode() {
		return call.hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitCommandAbortAck(this, param);
	}

}
