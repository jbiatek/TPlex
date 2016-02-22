package edu.umn.crisys.plexil.expr.ast;

import java.util.Optional;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
	private Optional<NodeRefExpr> direction;
	
	public NodeIDExpression(String nodeId) {
		this(nodeId, Optional.empty());
	}
	
    public NodeIDExpression(String nodeId, Optional<NodeRefExpr> direction) {
        super(nodeId, PlexilType.NODEREF);
        this.direction = direction;
    }
    
    public Optional<NodeRefExpr> getDirection() {
    	return direction;
    }

}
