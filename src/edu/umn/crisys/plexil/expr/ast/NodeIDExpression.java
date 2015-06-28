package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprType;

public class NodeIDExpression extends UnresolvedVariableExpr {
    
    public NodeIDExpression(String nodeId) {
        super(nodeId, ExprType.NODEREF);
    }

}
