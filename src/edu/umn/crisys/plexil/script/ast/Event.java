package edu.umn.crisys.plexil.script.ast;

public interface Event {
	public <P,R> R accept(ScriptEventVisitor<P,R> v, P param);
}