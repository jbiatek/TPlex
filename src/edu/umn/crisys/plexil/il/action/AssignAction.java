package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.ast.expr.ILExpression;

public class AssignAction implements PlexilAction {

    private ILExpression lhs;
    private ILExpression rhs;
    private int priority;
    
    
    public AssignAction(ILExpression lhs, ILExpression rhs, int priority) {
    	if ( ! lhs.isAssignable()) {
    		throw new RuntimeException(lhs+" is not assignable!");
    	}
        this.lhs = lhs;
        this.rhs = rhs;
        this.priority = priority;
    }
    
    public ILExpression getRHS() {
        return rhs;
    }

    @Override
    public String toString() {
        return "Assignment: "+lhs+" = "+rhs;
    }

	public ILExpression getLHS() {
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
