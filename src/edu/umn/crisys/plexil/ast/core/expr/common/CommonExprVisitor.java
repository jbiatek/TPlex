package edu.umn.crisys.plexil.ast.core.expr.common;

import edu.umn.crisys.plexil.java.values.PValueVisitor;


public interface CommonExprVisitor<Param, Return> extends PValueVisitor<Param, Return> {
    
    public Return visitArrayIndex(ArrayIndexExpr array, Param param);
    
    public Return visitLookupNow(LookupNowExpr lookup, Param param);
    
    public Return visitLookupOnChange(LookupOnChangeExpr lookup, Param param);
    
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);
    
    public Return visitOperation(Operation op, Param param);
    
}
