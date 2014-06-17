package edu.umn.crisys.plexil.script.ast;

public class Delay implements Event {
	
	public static final Delay SINGLETON = new Delay();
	
	private Delay() {}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		// TODO Auto-generated method stub
		return v.visitDelay(this, param);
	}
    
}