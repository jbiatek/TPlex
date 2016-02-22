package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.CascadingExprVisitor;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.NativeBool;

public abstract class ASTExprVisitor<P, R> implements CascadingExprVisitor<P, R>{

	private final R visitILExpression(ILExpr e) {
		throw new RuntimeException("This is an IL expression: "+e);
	}
	
	public final R visit(GetNodeStateExpr state, P param){
		return visitILExpression(state);
	}
	public final R visit(AliasExpr alias, P param) {
		return visitILExpression(alias);
	}
    public final R visit(RootAncestorExpr root, P param) {
		return visitILExpression(root);
    }
	public final R visit(SimpleVar var, P param) {
		return visitILExpression(var);
	}
	public final R visit(ArrayVar array, P param) {
		return visitILExpression(array);
	}
	public final R visit(LibraryVar lib, P param) {
		return visitILExpression(lib);
	}

	@Override
	public final R visit(NativeBool b, P param) {
		return visitILExpression(b);
	}

	@Override
	public final R visit(ILOperation op, P param) {
		return visitILExpression(op);
	}

}
