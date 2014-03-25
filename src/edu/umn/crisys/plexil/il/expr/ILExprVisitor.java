package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.vars.ILVarVisitor;

public interface ILExprVisitor<Param, Return> 
extends CommonExprVisitor<Param, Return>, ILVarVisitor<Param, Return>
{

	public Return visitGetNodeState(GetNodeStateExpr state, Param param);
	public Return visitAlias(AliasExpr alias, Param param);
    public Return visitRootParentState(RootParentStateExpr state, Param param);
    public Return visitRootParentExit(RootAncestorExitExpr ancExit, Param param);
    public Return visitRootParentEnd(RootAncestorEndExpr ancEnd, Param param);
    public Return visitRootParentInvariant(RootAncestorInvariantExpr ancInv, Param param);
    
}
