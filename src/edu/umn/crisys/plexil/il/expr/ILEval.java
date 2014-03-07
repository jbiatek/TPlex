package edu.umn.crisys.plexil.il.expr;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation.Operator;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

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
	public PValue visitArrayLiteral(ArrayLiteralExpr array, Void param) {
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
	public PValue visitNodeTimepoint(NodeTimepointExpr timept, Void param) {
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
	public PValue visitPValue(PValueExpression value, Void param) {
		return value.getValue();
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
	public PValue visitVariable(IntermediateVariable var, Void param) {
		return UnknownValue.get();
	}
	
}