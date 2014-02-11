package edu.umn.crisys.plexil.ast.core.visitor;

import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;

public interface CommonExprVisitor<Param, Return> {

    
    public Return visitArrayIndex(ArrayIndexExpr array, Param param);
    
    public Return visitArrayLiteral(ArrayLiteralExpr array, Param param);
    
    public Return visitLookupNow(LookupNowExpr lookup, Param param);
    
    public Return visitLookupOnChange(LookupOnChangeExpr lookup, Param param);
    
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);
    
    public Return visitOperation(Operation op, Param param);
    
    public Return visitPValue(PValueExpression value, Param param);
}
