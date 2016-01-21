package edu.umn.crisys.plexil.expr;

import edu.umn.crisys.plexil.expr.ast.ASTLookupExpr;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
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
public interface CascadingExprVisitor<P, R> extends ExprVisitor<P,R> {
	
	public default R visit(Expression e, P param) {
		throw new RuntimeException("This visitor does not support "
				+e.getClass().getSimpleName());
	}
    
	
	//PValues
	public default R visit(PValue v, P param) {
		return visit((Expression) v, param);
	}
	public default R visit(BooleanValue bool, P param) {
		return visit((PValue)bool, param);
	}
	public default R visit(IntegerValue integer, P param) {
		return visit((PValue)integer, param);
	}
	public default R visit(RealValue real, P param) {
		return visit((PValue)real, param);
	}
	public default R visit(StringValue string, P param) {
		return visit((PValue)string, param);
	}
	public default R visit(UnknownValue unk, P param) {
		return visit((PValue)unk, param);
	}
	public default R visit(PValueList<?> list, P param) {
		return visit((PValue)list, param);
	}
	public default R visit(CommandHandleState state, P param) {
		return visit((PValue)state, param);
	}
	public default R visit(NodeFailureType type, P param) {
		return visit((PValue)type, param);
	}
	public default R visit(NodeOutcome outcome, P param) {
		return visit((PValue)outcome, param);
	}
	public default R visit(NodeState state, P param) {
		return visit((PValue)state, param);
	}

	// Lookups
	public default R visit(LookupExpr lookup, P param) {
		return visit((Expression)lookup, param);
	}
    public default R visit(ASTLookupExpr lookup, P param) {
		return visit((Expression)lookup, param);
	}
    public default R visit(ASTOperation op, P param) {
		return visit((Expression)op, param);
	}

    // AST expressions
    public default R visit(UnresolvedVariableExpr expr, P param) {
		return visit((Expression)expr, param);
	}
    public default R visit(NodeRefExpr ref, P param) {
		return visit((Expression)ref, param);
	}
    public default R visit(NodeTimepointExpr timept, P param) {
		return visit((Expression)timept, param);
	}

    
    // IL expressions
    public default R visit(NamedCondition named, P param) {
    	return visit((Expression) named, param);
    }
	public default R visit(GetNodeStateExpr state, P param) {
		return visit((Expression)state, param);
	}
	public default R visit(AliasExpr alias, P param) {
		return visit((Expression)alias, param);
	}
    public default R visit(RootAncestorExpr root, P param) {
		return visit((Expression)root, param);
	}
	default R visit(NativeBool b, P param) {
		return visit((Expression)b, param);
	}
	default R visit(ILOperation op, P param) {
		return visit((Expression)op, param);
	}


	//ILVariables
    public default R visit(ILVariable v, P param) {
    	return visit((Expression)v, param);
    }
	public default R visit(SimpleVar var, P param) {
		return visit((ILVariable)var, param);
	}
	public default R visit(ArrayVar array, P param) {
		return visit((ILVariable)array, param);
	}
	public default R visit(LibraryVar lib, P param) {
		return visit((ILVariable)lib, param);
	}

    
}
