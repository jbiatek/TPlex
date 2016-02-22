package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILExprBase;
import edu.umn.crisys.plexil.expr.il.ILType;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;

public class NodeTimepointExpr extends ILExprBase {

    private NodeState state;
    public NodeState getState() { return state; }
    
    private NodeTimepoint point;
    public NodeTimepoint getTimepoint() { return point; }
    
    private ILExpr nodeId;
    public ILExpr getNodeId() { return nodeId; }
    
    public NodeTimepointExpr(NodeState state, NodeTimepoint timepoint, ILExpr nodeId) {
    	super(NodeToIL.TIMEPOINT_TYPE);
        this.state = state;
        this.point = timepoint;
        ILType.NODEREF.typeCheck(nodeId.getType());
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
