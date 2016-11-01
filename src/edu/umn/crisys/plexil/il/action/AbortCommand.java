package edu.umn.crisys.plexil.il.action;

public class AbortCommand implements PlexilAction {

	private CommandAction originalCommand;
	
	public AbortCommand(CommandAction originalCommand) {
		this.originalCommand = originalCommand;
	}
	
	public CommandAction getOriginalCommand() { 
		return originalCommand;
	}
	
	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitAbortCommand(this, param);
	}

}
