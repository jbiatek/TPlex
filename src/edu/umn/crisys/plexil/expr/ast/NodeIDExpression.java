package edu.umn.crisys.plexil.expr.ast;

import java.util.Optional;

import edu.umn.crisys.plexil.expr.il.ILType;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
	private Optional<NodeRefExpr> direction;
	
	public NodeIDExpression(String nodeId) {
		this(nodeId, Optional.empty());
	}
	
    public NodeIDExpression(String nodeId, Optional<NodeRefExpr> direction) {
        super(nodeId, ILType.NODEREF);
        this.direction = direction;
    }
    
    public Optional<NodeRefExpr> getDirection() {
    	return direction;
    }

}
