package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.expr.il.ILExpr;

public class AssignAction implements PlexilAction {

    private ILExpr lhs;
    private ILExpr rhs;
    private int priority;
    
    
    public AssignAction(ILExpr lhs, ILExpr rhs, int priority) {
    	if ( ! lhs.isAssignable()) {
    		throw new RuntimeException(lhs+" is not assignable!");
    	}
        this.lhs = lhs;
        this.rhs = rhs;
        this.priority = priority;
    }
    
    public ILExpr getRHS() {
        return rhs;
    }

    @Override
    public String toString() {
        return "Assignment: "+lhs+" = "+rhs;
    }

	public ILExpr getLHS() {
		return lhs;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitAssign(this, param);
	}


}
