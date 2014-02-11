package edu.umn.crisys.plexil.ast.core.expr.var;

import edu.umn.crisys.plexil.java.values.PlexilType;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
    public NodeIDExpression(String nodeId) {
        super(nodeId, PlexilType.NODEREF);
    }

}
