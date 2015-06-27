package edu.umn.crisys.plexil.il.expr;


import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class GetNodeStateExpr implements Expression {

    private NodeUID nodeUniquePath;
    
    public GetNodeStateExpr(NodeUID nodeId) {
        this.nodeUniquePath = nodeId;
    }
    
    public NodeUID getNodeUid() {
    	return nodeUniquePath;
    }

    @Override
    public String toString() {
        return nodeUniquePath+".state";
    }

    @Override
    public PlexilType getType() {
        return PlexilType.STATE;
    }

    /*
    public JExpression rhs(JCodeModel cm) {
        return JExpr.invoke(StateMachineToJava.getMappingMethodName(nodeUniquePath));
    }*/

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
        return visitor.visitGetNodeState(this, param);
    }

	@Override
	public boolean isAssignable() {
		return false;
	}

}
