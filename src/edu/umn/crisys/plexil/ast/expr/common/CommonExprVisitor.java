package edu.umn.crisys.plexil.ast.expr.common;

import edu.umn.crisys.plexil.ast.expr.Expression;
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
    
	public default R visitBooleanValue(BooleanValue bool, P param) {
		return visitUnsupported((Expression)bool, param);
	}
	public default R visitIntegerValue(IntegerValue integer, P param) {
		return visitUnsupported((Expression)integer, param);
	}
	public default R visitRealValue(RealValue real, P param) {
		return visitUnsupported((Expression)real, param);
	}
	public default R visitStringValue(StringValue string, P param) {
		return visitUnsupported((Expression)string, param);
	}
	public default R visitUnknownValue(UnknownValue unk, P param) {
		return visitUnsupported((Expression)unk, param);
	}
	public default R visitPValueList(PValueList<?> list, P param) {
		return visitUnsupported((Expression)list, param);
	}
	public default R visitCommandHandleState(CommandHandleState state, P param) {
		return visitUnsupported((Expression)state, param);
	}
	public default R visitNodeFailure(NodeFailureType type, P param) {
		return visitUnsupported((Expression)type, param);
	}
	public default R visitNodeOutcome(NodeOutcome outcome, P param) {
		return visitUnsupported((Expression)outcome, param);
	}
	public default R visitNodeState(NodeState state, P param) {
		return visitUnsupported((Expression)state, param);
	}

    public default R visitArrayIndex(ArrayIndexExpr array, P param) {
		return visitUnsupported((Expression)array, param);
	}
    public default R visitLookupNow(LookupNowExpr lookup, P param) {
		return visitUnsupported((Expression)lookup, param);
	}
    public default R visitLookupOnChange(LookupOnChangeExpr lookup, P param) {
		return visitUnsupported((Expression)lookup, param);
	}
    public default R visitOperation(Operation op, P param) {
		return visitUnsupported((Expression)op, param);
	}

    // AST expressions
    public default R visitVariable(UnresolvedVariableExpr expr, P param) {
		return visitUnsupported((Expression)expr, param);
	}
    public default R visitNodeReference(NodeRefExpr ref, P param) {
		return visitUnsupported((Expression)ref, param);
	}
    public default R visitDefaultEnd(DefaultEndExpr end, P param) {
		return visitUnsupported((Expression)end, param);
	}
    public default R visitNodeTimepoint(NodeTimepointExpr timept, P param) {
		return visitUnsupported((Expression)timept, param);
	}

    
    // IL expressions
	public default R visitGetNodeState(GetNodeStateExpr state, P param) {
		return visitUnsupported((Expression)state, param);
	}
	public default R visitAlias(AliasExpr alias, P param) {
		return visitUnsupported((Expression)alias, param);
	}
    public default R visitRootParentState(RootParentStateExpr state, P param) {
		return visitUnsupported((Expression)state, param);
	}
    public default R visitRootParentExit(RootAncestorExitExpr ancExit, P param) {
		return visitUnsupported((Expression)ancExit, param);
	}
    public default R visitRootParentEnd(RootAncestorEndExpr ancEnd, P param) {
		return visitUnsupported((Expression)ancEnd, param);
	}
    public default R visitRootParentInvariant(RootAncestorInvariantExpr ancInv, P param) {
		return visitUnsupported((Expression)ancInv, param);
	}

	public default R visitSimple(SimpleVar var, P param) {
		return visitUnsupported((Expression)var, param);
	}
	public default R visitArray(ArrayVar array, P param) {
		return visitUnsupported((Expression)array, param);
	}
	public default R visitLibrary(LibraryVar lib, P param) {
		return visitUnsupported((Expression)lib, param);
	}

    
}
