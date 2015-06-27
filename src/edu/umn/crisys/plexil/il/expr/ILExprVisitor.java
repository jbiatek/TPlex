package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;

public interface ILExprVisitor<P, R> 
extends CommonExprVisitor<P, R>, ILVarVisitor<P, R>
{

    public default R visitVariable(UnresolvedVariableExpr expr, P param) {
    	throw new RuntimeException("This is an AST expression: "+expr);
    }
    public default R visitNodeReference(NodeRefExpr ref, P param) {
    	throw new RuntimeException("This is an AST expression: "+ref);

    }
    public default R visitDefaultEnd(DefaultEndExpr end, P param) {
    	throw new RuntimeException("This is an AST expression: "+end);

    }
    public default R visitNodeTimepoint(NodeTimepointExpr timept, P param) {
    	throw new RuntimeException("This is an AST expression: "+timept);
    }

    
}
