package edu.umn.crisys.plexil.il.expr;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeConstant;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeEqual;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExprVisitor;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeOperation;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
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

/**
 * A superclass for visitors that modify ILExpressions. Each method implemented
 * here either returns the expression unmodified, or if it is a composite, 
 * recurses down the tree and returns the same type of expression with the new
 * arguments, if any. All you have to do is override the methods for the
 * type of expression that you want to change, and everything else should be
 * left untouched. 
 * 
 * @author jbiatek
 *
 * @param <Param>
 */
public abstract class ILExprModifier<Param> implements ILExprVisitor<Param, Expression>, NativeExprVisitor<Param, NativeExpr> {

	@Override
	public NativeExpr visitNativeConstant(NativeConstant c, Param param) {
		return c;
	}

	@Override
	public NativeExpr visitNativeOperation(NativeOperation op, Param param) {
		List<NativeExpr> newArgs = new ArrayList<>();
		for (NativeExpr oldArg : op.getArgs()) {
			newArgs.add(oldArg.accept(this, param));
		}
		return new NativeOperation(op.getOperation(), newArgs);
	}

	@Override
	public NativeExpr visitPlexilExprToNative(PlexilExprToNative pen,
			Param param) {
		return new PlexilExprToNative(
				pen.getPlexilExpr().accept(this, param), 
				pen.getCondition());
	}
	
	@Override
	public NativeExpr visitNativeEqual(NativeEqual e, Param param) {
		return new NativeEqual(e.getLeft().accept(this, param), 
				e.getRight().accept(this, param));
	}

	public Expression visitComposite(Expression composite, Param param) {
		List<Expression> modified = new ArrayList<Expression>();
		for (Expression child : composite.getArguments()) {
			modified.add(child.accept(this, param));
		}
		return composite.getCloneWithArgs(modified);
	}
	
	@Override
	public Expression visitArrayIndex(ArrayIndexExpr array, Param param) {
		return visitComposite(array, param);
	}

	@Override
	public Expression visitLookupNow(LookupNowExpr lookup, Param param) {
		return visitComposite(lookup, param);
	}

	@Override
	public Expression visitLookupOnChange(LookupOnChangeExpr lookup,
			Param param) {
		return visitComposite(lookup, param);
	}

	@Override
	public Expression visitOperation(Operation op, Param param) {
		return visitComposite(op, param);
	}

	@Override
	public Expression visitBooleanValue(BooleanValue bool, Param param) {
		return bool;
	}

	@Override
	public Expression visitIntegerValue(IntegerValue integer, Param param) {
		return integer;
	}

	@Override
	public Expression visitRealValue(RealValue real, Param param) {
		return real;
	}

	@Override
	public Expression visitStringValue(StringValue string, Param param) {
		return string;
	}

	@Override
	public Expression visitUnknownValue(UnknownValue unk, Param param) {
		return unk;
	}

	@Override
	public Expression visitPValueList(PValueList<?> list, Param param) {
		return list;
	}

	@Override
	public Expression visitCommandHandleState(CommandHandleState state,
			Param param) {
		return state;
	}

	@Override
	public Expression visitNodeFailure(NodeFailureType type, Param param) {
		return type;
	}

	@Override
	public Expression visitNodeOutcome(NodeOutcome outcome, Param param) {
		return outcome;
	}

	@Override
	public Expression visitNodeState(NodeState state, Param param) {
		return state;
	}

	@Override
	public Expression visitSimple(SimpleVar var, Param param) {
		return var;
	}

	@Override
	public Expression visitArray(ArrayVar array, Param param) {
		return array;
	}

	@Override
	public Expression visitLibrary(LibraryVar lib, Param param) {
		return lib;
	}

	@Override
	public Expression visitGetNodeState(GetNodeStateExpr state, Param param) {
		return state;
	}

	@Override
	public Expression visitAlias(AliasExpr alias, Param param) {
		return alias;
	}

	@Override
	public Expression visitRootParentState(RootParentStateExpr state,
			Param param) {
		return state;
	}

	@Override
	public Expression visitRootParentExit(RootAncestorExitExpr ancExit,
			Param param) {
		return ancExit;
	}

	@Override
	public Expression visitRootParentEnd(RootAncestorEndExpr ancEnd,
			Param param) {
		return ancEnd;
	}

	@Override
	public Expression visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Param param) {
		return ancInv;
	}

}
