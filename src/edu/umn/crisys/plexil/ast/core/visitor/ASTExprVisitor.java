package edu.umn.crisys.plexil.ast.core.visitor;

import edu.umn.crisys.plexil.ast.core.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.core.expr.var.UnresolvedVariableExpr;

public interface ASTExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

    public Return visitVariable(UnresolvedVariableExpr expr, Param param);
    
    public Return visitDefaultEnd(DefaultEndExpr end, Param param);
    
}
