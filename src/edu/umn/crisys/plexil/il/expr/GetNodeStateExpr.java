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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nodeUniquePath == null) ? 0 : nodeUniquePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(ILExpr e) {
		if (this == e)
			return true;
		if (getClass() != e.getClass())
			return false;
		GetNodeStateExpr other = (GetNodeStateExpr) e;
		if (nodeUniquePath == null) {
			if (other.nodeUniquePath != null)
				return false;
		} else if (!nodeUniquePath.equals(other.nodeUniquePath))
			return false;
		return true;
	}

}
