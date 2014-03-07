package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.translator.il.vars.CommandHandleReference;

public class AbortCommandAction implements PlexilAction {

    private CommandHandleReference handle;
    
    public AbortCommandAction(CommandHandleReference handle) {
        this.handle = handle;
    }

    @Override
    public String toString() {
        return "Action: Abort command for handle "+handle;
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitAbortCommand(this, param);
	}
    
}
