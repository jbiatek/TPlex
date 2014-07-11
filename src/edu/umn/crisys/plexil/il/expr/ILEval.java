package edu.umn.crisys.plexil.il.expr;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.expr.common.Operation;
import edu.umn.crisys.plexil.ast.expr.common.Operation.Operator;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
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

public class ILEval implements ILExprVisitor<Void, PValue> {

    public static boolean isConstant(ILExpression ilExpr) {
        return eval(ilExpr).isKnown();
    }
    
    public static PValue eval(ILExpression ilExpr) {
        return ilExpr.accept(new ILEval(), null);
    }

	@Override
	public PValue visitArrayIndex(ArrayIndexExpr array, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitLookupNow(LookupNowExpr lookup, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitLookupOnChange(LookupOnChangeExpr lookup, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitOperation(Operation op, Void param) {
		// Normally, we just call variables UNKNOWN and everything works
		// out because we really don't know what they'll be. But there's
		// one exception to this...
		if (op.getOperator() == Operator.ISKNOWN) {
			PValue arg = op.getArguments().get(0).accept(this, null);
			if (arg.isUnknown()) {
				// Since Expressions can't reference UNKNOWN except through
				// variables, and we don't know if this variable will always
				// be unknown or not, the answer is actually UNKNOWN. 
				return UnknownValue.get();
			} else {
				// It must be a constant? This probably doesn't make
				// any actual sense. 
				return BooleanValue.get(true);
			}
		}
		
		List<PValue> values = new ArrayList<PValue>();
		for (Expression arg : op.getArguments()) {
			values.add(arg.accept(this, null));
		}
		return op.getOperator().eval(values);
	}

	@Override
	public PValue visitRootParentState(RootParentStateExpr state, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitRootParentEnd(RootAncestorEndExpr ancEnd, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		return UnknownValue.get();
	}


	@Override
	public PValue visitSimple(SimpleVar var, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitArray(ArrayVar array, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitLibrary(LibraryVar lib, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitAlias(AliasExpr alias, Void param) {
		return UnknownValue.get();
	}

	@Override
	public PValue visitGetNodeState(GetNodeStateExpr state, Void param) {
		return UnknownValue.get();
	}
	
	@Override
	public PValue visitBooleanValue(BooleanValue bool, Void param) {
		return bool;
	}

	@Override
	public PValue visitIntegerValue(IntegerValue integer, Void param) {
		return integer;
	}

	@Override
	public PValue visitRealValue(RealValue real, Void param) {
		return real;
	}

	@Override
	public PValue visitStringValue(StringValue string, Void param) {
		return string;
	}

	@Override
	public PValue visitUnknownValue(UnknownValue unk, Void param) {
		return unk;
	}

	@Override
	public PValue visitPValueList(PValueList<?> list, Void param) {
		return list;
	}

	@Override
	public PValue visitCommandHandleState(CommandHandleState state, Void param) {
		return state;
	}

	@Override
	public PValue visitNodeFailure(NodeFailureType type, Void param) {
		return type;
	}

	@Override
	public PValue visitNodeOutcome(NodeOutcome outcome, Void param) {
		return outcome;
	}

	@Override
	public PValue visitNodeState(NodeState state, Void param) {
		return state;
	}

	
}