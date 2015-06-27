package edu.umn.crisys.plexil.ast.expr.common;

import cute.concolic.symbolicstate.Expression;
import edu.umn.crisys.plexil.ast.expr.var.DefaultEndExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeRefExpr;
import edu.umn.crisys.plexil.ast.expr.var.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.expr.var.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;


public interface CommonExprVisitor<Param, Return> {
	
	public default Return visitUnsupported(Expression e, Param param) {
		throw new RuntimeException("This visitor does not support "
				+e.getClass().getSimpleName());
	}
    
	public Return visitBooleanValue(BooleanValue bool, Param param);
	public Return visitIntegerValue(IntegerValue integer, Param param);
	public Return visitRealValue(RealValue real, Param param);
	public Return visitStringValue(StringValue string, Param param);
	public Return visitUnknownValue(UnknownValue unk, Param param);
	public Return visitPValueList(PValueList<?> list, Param param);
	public Return visitCommandHandleState(CommandHandleState state, Param param);
	public Return visitNodeFailure(NodeFailureType type, Param param);
	public Return visitNodeOutcome(NodeOutcome outcome, Param param);
	public Return visitNodeState(NodeState state, Param param);

    public Return visitArrayIndex(ArrayIndexExpr array, Param param);
    public Return visitLookupNow(LookupNowExpr lookup, Param param);
    public Return visitLookupOnChange(LookupOnChangeExpr lookup, Param param);
    public Return visitOperation(Operation op, Param param);

    // AST expressions
    public Return visitVariable(UnresolvedVariableExpr expr, Param param);
    public Return visitNodeReference(NodeRefExpr ref, Param param);
    public Return visitDefaultEnd(DefaultEndExpr end, Param param);
    public Return visitNodeTimepoint(NodeTimepointExpr timept, Param param);

    
    // IL expressions
	public Return visitGetNodeState(GetNodeStateExpr state, Param param);
	public Return visitAlias(AliasExpr alias, Param param);
    public Return visitRootParentState(RootParentStateExpr state, Param param);
    public Return visitRootParentExit(RootAncestorExitExpr ancExit, Param param);
    public Return visitRootParentEnd(RootAncestorEndExpr ancEnd, Param param);
    public Return visitRootParentInvariant(RootAncestorInvariantExpr ancInv, Param param);

	public Return visitSimple(SimpleVar var, Param param);
	public Return visitArray(ArrayVar array, Param param);
	public Return visitLibrary(LibraryVar lib, Param param);

    
}
