package edu.umn.crisys.plexil.ast.core.expr.common;


public interface CommonExprVisitor<Param, Return> {

    
    public Return visitArrayIndex(ArrayIndexExpr array, Param param);
    
    public Return visitArrayLiteral(ArrayLiteralExpr array, Param param);
    
    public Return visitLookupNow(LookupNowExpr lookup, Param param);
    
    public Return visitLookupOnChange(LookupOnChangeExpr lookup, Param param);
    
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);
    
    public Return visitOperation(Operation op, Param param);
    
    public Return visitPValue(PValueExpression value, Param param);
}
