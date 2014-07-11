package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
    public NodeIDExpression(String nodeId) {
        super(nodeId, PlexilType.NODEREF);
    }

}
