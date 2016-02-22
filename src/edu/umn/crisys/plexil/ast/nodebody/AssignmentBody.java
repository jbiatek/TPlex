package edu.umn.crisys.plexil.ast.nodebody;

import edu.umn.crisys.plexil.expr.il.ILExpr;

public class AssignmentBody extends NodeBody {

    private ILExpr leftHandSide;
    private ILExpr rightHandSide;
    
    public AssignmentBody(ILExpr leftHandSide, ILExpr rightHandSide) {
    	if ( ! leftHandSide.isAssignable()) {
    		throw new RuntimeException(leftHandSide +" is not assignable.");
    	}
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }
    
    public ILExpr getLeftHandSide() {
        return leftHandSide;
    }
    
    public ILExpr getRightHandSide() {
        return rightHandSide;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitAssignment(this, param);
    }
}
