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

public interface ASTExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

	public default Return visitGetNodeState(GetNodeStateExpr state, Param param){
		throw new RuntimeException("This is an IL expression: "+state);
	}
	public default Return visitAlias(AliasExpr alias, Param param) {
		throw new RuntimeException("This is an IL expression: "+alias);
	}
    public default Return visitRootParentState(RootParentStateExpr state, Param param) {
		throw new RuntimeException("This is an IL expression: "+state);
    }
    public default Return visitRootParentExit(RootAncestorExitExpr ancExit, Param param) {
		throw new RuntimeException("This is an IL expression: "+ancExit);
    }
    public default Return visitRootParentEnd(RootAncestorEndExpr ancEnd, Param param) {
		throw new RuntimeException("This is an IL expression: "+ancEnd);
    }
    public default Return visitRootParentInvariant(RootAncestorInvariantExpr ancInv, Param param) {
		throw new RuntimeException("This is an IL expression: "+ancInv);
    }
	public default Return visitSimple(SimpleVar var, Param param) {
		throw new RuntimeException("This is an IL expression: "+var);
	}
	public default Return visitArray(ArrayVar array, Param param) {
		throw new RuntimeException("This is an IL expression: "+array);
	}
	public default Return visitLibrary(LibraryVar lib, Param param) {
		throw new RuntimeException("This is an IL expression: "+lib);
	}

}
