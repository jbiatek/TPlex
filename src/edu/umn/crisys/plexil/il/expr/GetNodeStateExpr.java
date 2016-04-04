package edu.umn.crisys.plexil.il.expr;


import edu.umn.crisys.plexil.il.NodeUID;

public class GetNodeStateExpr extends ILExprBase {

    private NodeUID nodeUniquePath;
    
    public GetNodeStateExpr(NodeUID nodeId) {
    	super(ILType.STATE);
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