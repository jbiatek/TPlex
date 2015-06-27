package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;

public abstract class ILExprVisitor<P, R> 
implements CommonExprVisitor<P, R>, ILVarVisitor<P, R>
{
	
	private R visitASTExpr(Expression e) {
		throw new RuntimeException("This is an AST expression: "+e);
	}

    public final R visitVariable(UnresolvedVariableExpr expr, P param) {
    	return visitASTExpr(expr);
    }
    public final R visitNodeReference(NodeRefExpr ref, P param) {
    	return visitASTExpr(ref);

    }
    public final R visitDefaultEnd(DefaultEndExpr end, P param) {
    	return visitASTExpr(end);

    }
    public final R visitNodeTimepoint(NodeTimepointExpr timept, P param) {
    	return visitASTExpr(timept);
    }

    
}
