package edu.umn.crisys.plexil.ast.core.expr.var;

import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;

public interface ASTExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

    public Return visitVariable(UnresolvedVariableExpr expr, Param param);
    public Return visitNodeReference(NodeRefExpr ref, Param param);
    public Return visitDefaultEnd(DefaultEndExpr end, Param param);
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);

}
