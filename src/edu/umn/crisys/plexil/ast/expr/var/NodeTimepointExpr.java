package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class NodeTimepointExpr implements ASTExpression {

    private NodeState state;
    public NodeState getState() { return state; }
    
    private NodeTimepoint point;
    public NodeTimepoint getTimepoint() { return point; }
    
    private Expression nodeId;
    public Expression getNodeId() { return nodeId; }
    
    public NodeTimepointExpr(NodeState state, NodeTimepoint timepoint, Expression nodeId) {
        this.state = state;
        this.point = timepoint;
        PlexilType.NODEREF.typeCheck(nodeId.getType());
        this.nodeId = nodeId;
    }
    
    public NodeTimepointExpr(String state, String timepoint, String nodeId) {
        this(   NodeState.valueOf(state.toUpperCase()), 
                NodeTimepoint.valueOf(timepoint.toUpperCase()), 
                new NodeIDExpression(nodeId));
    }
    
    @Override
    public String toString() {
        return nodeId+"."+state+"."+point;
    }

    @Override
    public String asString() { return this.toString(); }

    @Override
    public PlexilType getType() {
        return PlexilType.REAL;
    }

    @Override
	public boolean isAssignable() {
		return false;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return visitor.visitNodeTimepoint(this, param);
	}
}
