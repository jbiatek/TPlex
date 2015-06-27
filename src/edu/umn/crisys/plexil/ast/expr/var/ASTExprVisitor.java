package edu.umn.crisys.plexil.ast.expr.var;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;

public interface ASTExprVisitor<P, R> extends CommonExprVisitor<P, R>{

	public default R visitGetNodeState(GetNodeStateExpr state, P param){
		throw new RuntimeException("This is an IL expression: "+state);
	}
	public default R visitAlias(AliasExpr alias, P param) {
		throw new RuntimeException("This is an IL expression: "+alias);
	}
    public default R visitRootParentState(RootParentStateExpr state, P param) {
		throw new RuntimeException("This is an IL expression: "+state);
    }
    public default R visitRootParentExit(RootAncestorExitExpr ancExit, P param) {
		throw new RuntimeException("This is an IL expression: "+ancExit);
    }
    public default R visitRootParentEnd(RootAncestorEndExpr ancEnd, P param) {
		throw new RuntimeException("This is an IL expression: "+ancEnd);
    }
    public default R visitRootParentInvariant(RootAncestorInvariantExpr ancInv, P param) {
		throw new RuntimeException("This is an IL expression: "+ancInv);
    }
	public default R visitSimple(SimpleVar var, P param) {
		throw new RuntimeException("This is an IL expression: "+var);
	}
	public default R visitArray(ArrayVar array, P param) {
		throw new RuntimeException("This is an IL expression: "+array);
	}
	public default R visitLibrary(LibraryVar lib, P param) {
		throw new RuntimeException("This is an IL expression: "+lib);
	}

}
