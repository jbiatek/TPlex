package edu.umn.crisys.plexil.ast.nodebody;

import edu.umn.crisys.plexil.expr.ast.PlexilExpr;

public class AssignmentBody extends NodeBody {

    private PlexilExpr leftHandSide;
    private PlexilExpr rightHandSide;
    
    public AssignmentBody(PlexilExpr leftHandSide, PlexilExpr rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }
    
    public PlexilExpr getLeftHandSide() {
        return leftHandSide;
    }
    
    public PlexilExpr getRightHandSide() {
        return rightHandSide;
    }
    
    @Override
    public <P,R> R accept(NodeBodyVisitor<P, R> visitor, P param) {
        return visitor.visitAssignment(this, param);
    }
}
