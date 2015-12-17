package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;

public class NodeTimepointExpr extends ExpressionBase {

    private NodeState state;
    public NodeState getState() { return state; }
    
    private NodeTimepoint point;
    public NodeTimepoint getTimepoint() { return point; }
    
    private Expression nodeId;
    public Expression getNodeId() { return nodeId; }
    
    public NodeTimepointExpr(NodeState state, NodeTimepoint timepoint, Expression nodeId) {
    	super(NodeToIL.TIMEPOINT_TYPE);
        this.state = state;
        this.point = timepoint;
        ExprType.NODEREF.typeCheck(nodeId.getType());
        this.nodeId = nodeId;
    }
    
    public NodeTimepointExpr(String state, String timepoint, String nodeId) {
        this(   NodeState.valueOf(state.toUpperCase()), 
                NodeTimepoint.valueOf(timepoint.toUpperCase()), 
                new NodeIDExpression(nodeId));
    }
    
    @Override
    public String asString() {
        return nodeId+"."+state+"."+point;
    }

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}
}
