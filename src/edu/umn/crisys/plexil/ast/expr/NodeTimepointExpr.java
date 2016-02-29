package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.NodeTimepoint;

public class NodeTimepointExpr extends PlexilExprBase {

    private NodeState state;
    public NodeState getState() { return state; }
    
    private NodeTimepoint point;
    public NodeTimepoint getTimepoint() { return point; }
    
    private PlexilExpr nodeId;
    public PlexilExpr getNodeId() { return nodeId; }
    
    public NodeTimepointExpr(NodeState state, NodeTimepoint timepoint, PlexilExpr nodeId) {
    	super(NodeToIL.TIMEPOINT_TYPE);
        this.state = state;
        this.point = timepoint;
        PlexilType.NODEREF.typeCheck(nodeId.getPlexilType());
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
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}
}
