package edu.umn.crisys.plexil.ast.core.visitor;

import edu.umn.crisys.plexil.translator.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public interface ILExprVisitor<Param, Return> extends CommonExprVisitor<Param, Return>{

    public Return visitRootParentState(RootParentStateExpr state, Param param);
    public Return visitRootParentExit(RootAncestorExitExpr ancExit, Param param);
    public Return visitRootParentEnd(RootAncestorEndExpr ancEnd, Param param);
    public Return visitRootParentInvariant(RootAncestorInvariantExpr ancInv, Param param);
    
    /*public Return visitAliasedVariable(AliasedVariableReference alias, Param param);
    public Return visitArrayElement(ArrayElementReference element, Param param);
    public Return visitArray(ArrayReference array, Param param);
    public Return visitCommandHandle(CommandHandleReference handle, Param param);
    public Return visitLibraryNode(LibraryNodeReference lib, Param param);
    public Return visitNodeTimepoint(NodeTimepointReference point, Param param);
    public Return visitPreviousValue(PreviousValueReference prev, Param param);
    public Return visitUpdateHandle(UpdateHandleReference update, Param param);
    public Return visitVariable(VariableReference var, Param param);*/
    
    public Return visitVariable(IntermediateVariable var, Param param);

}
