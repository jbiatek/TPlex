package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;

public interface ASTExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

    public Return visitVariable(UnresolvedVariableExpr expr, Param param);
    public Return visitNodeReference(NodeRefExpr ref, Param param);
    public Return visitDefaultEnd(DefaultEndExpr end, Param param);
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);

}
