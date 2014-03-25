package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.il.vars.LibraryVar;

public class RunLibraryNodeAction implements PlexilAction {
    
    private LibraryVar node;
    
    public RunLibraryNodeAction(LibraryVar node) {
        this.node = node;
    }
    
    public LibraryVar getLibNode() {
    	return node;
    }

    @Override
    public String toString() {
        return "Run library node "+node;
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitRunLibraryNode(this, param);
	}
}
