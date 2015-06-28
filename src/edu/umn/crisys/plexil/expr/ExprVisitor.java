package edu.umn.crisys.plexil.expr;

import edu.umn.crisys.plexil.expr.ast.DefaultEndExpr;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorEndExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorExitExpr;
import edu.umn.crisys.plexil.expr.il.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.expr.il.RootParentStateExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Visitor pattern for Expressions. By default, visitor methods return the 
 * results of a more general method: for example, if you don't implement the
 * method for BooleanValue, it will instead call the method for PValue. They
 * all lead eventually to the general "Expression" method. By default, this
 * catchall method throws an exception, but you could use it to, say, 
 * propagate this visitor to arguments of expression types that you don't 
 * care about or return a default value.  
 * 
 * @author jbiatek
 *
 * @param <P>
 * @param <R>
 */
public interface ExprVisitor<P, R> {
	
	public default R visitUnsupported(Expression e, P param) {
		throw new RuntimeException("This visitor does not support "
				+e.getClass().getSimpleName());
	}
    
	
	//PValues
	public default R visitPValue(PValue v, P param) {
		return visitUnsupported((PValue) v, param);
	}
	public default R visitBooleanValue(BooleanValue bool, P param) {
		return visitPValue((PValue)bool, param);
	}
	public default R visitIntegerValue(IntegerValue integer, P param) {
		return visitPValue((PValue)integer, param);
	}
	public default R visitRealValue(RealValue real, P param) {
		return visitPValue((PValue)real, param);
	}
	public default R visitStringValue(StringValue string, P param) {
		return visitPValue((PValue)string, param);
	}
	public default R visitUnknownValue(UnknownValue unk, P param) {
		return visitPValue((PValue)unk, param);
	}
	public default R visitPValueList(PValueList<?> list, P param) {
		return visitPValue((PValue)list, param);
	}
	public default R visitCommandHandleState(CommandHandleState state, P param) {
		return visitPValue((PValue)state, param);
	}
	public default R visitNodeFailure(NodeFailureType type, P param) {
		return visitPValue((PValue)type, param);
	}
	public default R visitNodeOutcome(NodeOutcome outcome, P param) {
		return visitPValue((PValue)outcome, param);
	}
	public default R visitNodeState(NodeState state, P param) {
		return visitPValue((PValue)state, param);
	}

	// Lookups
	public default R visitLookup(LookupExpr lookup, P param) {
		return visitUnsupported((Expression)lookup, param);
	}
    public default R visitLookupNow(LookupNowExpr lookup, P param) {
		return visitLookup((LookupExpr)lookup, param);
	}
    public default R visitLookupOnChange(LookupOnChangeExpr lookup, P param) {
		return visitLookup((LookupExpr)lookup, param);
	}
	
	
    public default R visitArrayIndex(ArrayIndexExpr array, P param) {
		return visitUnsupported((Expression)array, param);
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

    
    //ILVariables
    public default R visitILVar(ILVariable v, P param) {
    	return visitUnsupported((Expression)v, param);
    }
	public default R visitSimple(SimpleVar var, P param) {
		return visitILVar((ILVariable)var, param);
	}
	public default R visitArray(ArrayVar array, P param) {
		return visitILVar((ILVariable)array, param);
	}
	public default R visitLibrary(LibraryVar lib, P param) {
		return visitILVar((ILVariable)lib, param);
	}

    
}
