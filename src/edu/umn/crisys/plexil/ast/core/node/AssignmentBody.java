package edu.umn.crisys.plexil.ast.core.node;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.ast.core.visitor.NodeBodyVisitor;

public class AssignmentBody extends NodeBody {

    private Expression leftHandSide;
    private Expression rightHandSide;
    
    public AssignmentBody(UnresolvedVariableExpr leftHandSide, Expression rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }
    
    public AssignmentBody(ArrayIndexExpr leftHandSide, Expression rightHandSide) {
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
