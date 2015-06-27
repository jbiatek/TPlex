package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.ast.expr.Expression;

public class AssignAction implements PlexilAction {

    private Expression lhs;
    private Expression rhs;
    private int priority;
    
    
    public AssignAction(Expression lhs, Expression rhs, int priority) {
    	if ( ! lhs.isAssignable()) {
    		throw new RuntimeException(lhs+" is not assignable!");
    	}
        this.lhs = lhs;
        this.rhs = rhs;
        this.priority = priority;
    }
    
    public Expression getRHS() {
        return rhs;
    }

    @Override
    public String toString() {
        return "Assignment: "+lhs+" = "+rhs;
    }

	public Expression getLHS() {
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
