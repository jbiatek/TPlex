package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.translator.il.vars.LibraryNodeReference;

public class RunLibraryNodeAction implements PlexilAction {
    
    private LibraryNodeReference node;
    
    public RunLibraryNodeAction(LibraryNodeReference node) {
        this.node = node;
    }
    
    public LibraryNodeReference getLibNode() {
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
