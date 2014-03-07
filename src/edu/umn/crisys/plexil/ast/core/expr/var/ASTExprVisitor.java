package edu.umn.crisys.plexil.ast.core.expr.var;

import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;

public interface ASTExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

    public Return visitVariable(UnresolvedVariableExpr expr, Param param);
    
    public Return visitDefaultEnd(DefaultEndExpr end, Param param);
    
}
