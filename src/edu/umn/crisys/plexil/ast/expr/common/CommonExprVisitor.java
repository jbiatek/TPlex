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


public interface CommonExprVisitor<P, R> {
	
	public default R visitUnsupported(Expression e, P param) {
		throw new RuntimeException("This visitor does not support "
				+e.getClass().getSimpleName());
	}
    
	public R visitBooleanValue(BooleanValue bool, P param);
	public R visitIntegerValue(IntegerValue integer, P param);
	public R visitRealValue(RealValue real, P param);
	public R visitStringValue(StringValue string, P param);
	public R visitUnknownValue(UnknownValue unk, P param);
	public R visitPValueList(PValueList<?> list, P param);
	public R visitCommandHandleState(CommandHandleState state, P param);
	public R visitNodeFailure(NodeFailureType type, P param);
	public R visitNodeOutcome(NodeOutcome outcome, P param);
	public R visitNodeState(NodeState state, P param);

    public R visitArrayIndex(ArrayIndexExpr array, P param);
    public R visitLookupNow(LookupNowExpr lookup, P param);
    public R visitLookupOnChange(LookupOnChangeExpr lookup, P param);
    public R visitOperation(Operation op, P param);

    // AST expressions
    public R visitVariable(UnresolvedVariableExpr expr, P param);
    public R visitNodeReference(NodeRefExpr ref, P param);
    public R visitDefaultEnd(DefaultEndExpr end, P param);
    public R visitNodeTimepoint(NodeTimepointExpr timept, P param);

    
    // IL expressions
	public R visitGetNodeState(GetNodeStateExpr state, P param);
	public R visitAlias(AliasExpr alias, P param);
    public R visitRootParentState(RootParentStateExpr state, P param);
    public R visitRootParentExit(RootAncestorExitExpr ancExit, P param);
    public R visitRootParentEnd(RootAncestorEndExpr ancEnd, P param);
    public R visitRootParentInvariant(RootAncestorInvariantExpr ancInv, P param);

	public R visitSimple(SimpleVar var, P param);
	public R visitArray(ArrayVar array, P param);
	public R visitLibrary(LibraryVar lib, P param);

    
}
