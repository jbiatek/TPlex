package edu.umn.crisys.plexil.il.action;

public class EndMacroStep implements PlexilAction {
	
	private static EndMacroStep SINGLETON = new EndMacroStep();

	public static EndMacroStep get() { return SINGLETON; }

	private EndMacroStep() {}
	
	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitEndMacroStep(this, param);
	}

}
