package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.PlexilType;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
    public NodeIDExpression(String nodeId) {
        super(nodeId, PlexilType.NODEREF);
    }

}
