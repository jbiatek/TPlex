package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.translator.il.vars.NodeTimepointReference;

public class SetTimepointAction implements PlexilAction {
    
    private NodeTimepointReference nt;

    public SetTimepointAction(NodeTimepointReference nt) {
        this.nt = nt;
    }

    public NodeTimepointReference getTimepoint() {
    	return nt;
    }
    
    @Override
    public String toString() {
        return "Set "+nt.toString();
    }

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitSetTimepoint(this, param);
	}
}
