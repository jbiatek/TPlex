package edu.umn.crisys.plexil.ast.core.nodebody;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.globaldecl.VariableDecl;

public class AssignmentBody extends NodeBody {

    private Expression leftHandSide;
    private Expression rightHandSide;
    
    public AssignmentBody(Expression leftHandSide, Expression rightHandSide) {
    	if ( ! leftHandSide.isAssignable()) {
    		throw new RuntimeException(leftHandSide +" is not assignable.");
    	}
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }
    
    public Expression getLeftHandSide() {
        return leftHandSide;
    }
    
    public Expression getRightHandSide() {
        return rightHandSide;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitAssignment(this, param);
    }
}
