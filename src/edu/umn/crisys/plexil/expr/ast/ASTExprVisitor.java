package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorEndExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExitExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.expr.il.RootParentStateExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;

public abstract class ASTExprVisitor<P, R> implements ExprVisitor<P, R>{

	private final R visitILExpression(Expression e) {
		throw new RuntimeException("This is an IL expression: "+e);
	}
	
	public final R visitGetNodeState(GetNodeStateExpr state, P param){
		return visitILExpression(state);
	}
	public final R visitAlias(AliasExpr alias, P param) {
		return visitILExpression(alias);
	}
    public final R visitRootParentState(RootParentStateExpr state, P param) {
		return visitILExpression(state);
    }
    public final R visitRootParentExit(RootAncestorExitExpr ancExit, P param) {
		return visitILExpression(ancExit);
    }
    public final R visitRootParentEnd(RootAncestorEndExpr ancEnd, P param) {
		return visitILExpression(ancEnd);
    }
    public final R visitRootParentInvariant(RootAncestorInvariantExpr ancInv, P param) {
		return visitILExpression(ancInv);
    }
	public final R visitSimple(SimpleVar var, P param) {
		return visitILExpression(var);
	}
	public final R visitArray(ArrayVar array, P param) {
		return visitILExpression(array);
	}
	public final R visitLibrary(LibraryVar lib, P param) {
		return visitILExpression(lib);
	}

}
