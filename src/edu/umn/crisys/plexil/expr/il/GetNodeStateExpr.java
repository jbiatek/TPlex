package edu.umn.crisys.plexil.expr.il;


import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.il.NodeUID;

public class GetNodeStateExpr extends ExpressionBase {

    private NodeUID nodeUniquePath;
    
    public GetNodeStateExpr(NodeUID nodeId) {
    	super(ExprType.STATE);
        this.nodeUniquePath = nodeId;
    }
    
    public NodeUID getNodeUid() {
    	return nodeUniquePath;
    }

    @Override
    public String asString() {
        return nodeUniquePath+".state";
    }

    @Override
    public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
        return visitor.visit(this, param);
    }

}
