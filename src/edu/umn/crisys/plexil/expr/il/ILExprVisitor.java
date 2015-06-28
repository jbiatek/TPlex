package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVarVisitor;

public abstract class ILExprVisitor<P, R> 
implements ExprVisitor<P, R>, ILVarVisitor<P, R>
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
