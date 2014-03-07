package edu.umn.crisys.plexil.il.action;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public class AssignAction implements PlexilAction {

    private IntermediateVariable lhs;
    private ILExpression rhs;
    private int priority;
    
    
    public AssignAction(IntermediateVariable vr, ILExpression rhs, int priority) {
        this.lhs = vr;
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

	public IntermediateVariable getLHS() {
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
