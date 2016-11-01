package edu.umn.crisys.plexil.script.ast;

public interface ScriptEventVisitor<P,R> {

	public R visitCommandAck(CommandAck ack, P param);
	public R visitCommandReturn(CommandReturn ret, P param);
	public R visitCommandAbortAck(CommandAbortAck abort, P param);
	public R visitDelay(Delay d, P param);
	public R visitSimultaneous(Simultaneous sim, P param);
	public R visitStateChange(StateChange lookup, P param);
	public R visitUpdateAck(UpdateAck ack, P param);

}
