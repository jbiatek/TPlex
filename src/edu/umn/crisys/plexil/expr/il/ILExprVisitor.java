package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.CascadingExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;

public abstract class ILExprVisitor<P, R> 
implements CascadingExprVisitor<P, R>
{
	
	private R visitASTExpr(Expression e) {
		throw new RuntimeException("This is an AST expression: "+e);
	}

    public final R visit(UnresolvedVariableExpr expr, P param) {
    	return visitASTExpr(expr);
    }
    public final R visit(NodeRefExpr ref, P param) {
    	return visitASTExpr(ref);

    }
    public final R visit(DefaultEndExpr end, P param) {
    	return visitASTExpr(end);

    }
    public final R visit(NodeTimepointExpr timept, P param) {
    	return visitASTExpr(timept);
    }

    
}
