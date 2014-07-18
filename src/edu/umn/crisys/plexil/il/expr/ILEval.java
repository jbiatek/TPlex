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

public class ILEval implements ILExprVisitor<Void, edu.umn.crisys.plexil.il.expr.ILEval.EvalResult> {
	
	private static final ILEval EVALUATOR = new ILEval();
	private ILEval() {}
	
	/**
	 * You can basically ignore this, it's an internal thing. 
	 * @author jbiatek
	 *
	 */
	public static interface EvalResult {
		public boolean isConstant();
		public PValue getConstantValue();
	}
	
	private static class ConstantResult implements EvalResult {
		private PValue value;
		
		public ConstantResult(PValue value) {
			this.value = value;
		}

		@Override
		public boolean isConstant() {
			return true;
		}

		@Override
		public PValue getConstantValue() {
			return value;
		}
	}
	
	private static class VariableResult implements EvalResult {

		public static final VariableResult SINGLETON = new VariableResult();
		
		@Override
		public boolean isConstant() {
			return false;
		}

		@Override
		public PValue getConstantValue() {
			throw new RuntimeException("This expression isn't constant!");
		}
		
	}

    public static boolean isConstant(ILExpression ilExpr) {
        return ilExpr.accept(EVALUATOR, null).isConstant();
    }
    
    public static PValue eval(ILExpression ilExpr) {
        return ilExpr.accept(EVALUATOR, null).getConstantValue();
    }

	@Override
	public EvalResult visitArrayIndex(ArrayIndexExpr array, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitLookupNow(LookupNowExpr lookup, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitLookupOnChange(LookupOnChangeExpr lookup, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitOperation(Operation op, Void param) {
		// OR and AND have short circuiting, which is slightly different
		// from everything else. 
		PValue shortCircuiter = null;
		if (op.getOperator() == Operator.AND) {
			shortCircuiter = BooleanValue.get(false);
		} else if (op.getOperator() == Operator.OR) {
			shortCircuiter = BooleanValue.get(true);
		}
		
		List<PValue> values = new ArrayList<PValue>();
		for (Expression arg : op.getArguments()) {
			EvalResult argResult = arg.accept(this, null);
			if (argResult.isConstant()) {
				if (shortCircuiter != null && argResult.getConstantValue().equals(shortCircuiter)) {
					// We have short circuited! This is always going to be it.
					return argResult;
				} else {
					// Just a regular constant. 
					values.add(argResult.getConstantValue());
				}
			} else {
				// Not constant. Could be anything.
				return VariableResult.SINGLETON;
			}
		}
		// All the arguments were constant, so this should be constant too. 
		return new ConstantResult(op.getOperator().eval(values));
	}
	
	@Override
	public EvalResult visitRootParentState(RootParentStateExpr state, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitRootParentEnd(RootAncestorEndExpr ancEnd, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		return VariableResult.SINGLETON;
	}


	@Override
	public EvalResult visitSimple(SimpleVar var, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitArray(ArrayVar array, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitLibrary(LibraryVar lib, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitAlias(AliasExpr alias, Void param) {
		return VariableResult.SINGLETON;
	}

	@Override
	public EvalResult visitGetNodeState(GetNodeStateExpr state, Void param) {
		return VariableResult.SINGLETON;
	}
	
	@Override
	public EvalResult visitBooleanValue(BooleanValue bool, Void param) {
		return new ConstantResult(bool);
	}

	@Override
	public EvalResult visitIntegerValue(IntegerValue integer, Void param) {
		return new ConstantResult(integer);
	}

	@Override
	public EvalResult visitRealValue(RealValue real, Void param) {
		return new ConstantResult(real);
	}

	@Override
	public EvalResult visitStringValue(StringValue string, Void param) {
		return new ConstantResult(string);
	}

	@Override
	public EvalResult visitUnknownValue(UnknownValue unk, Void param) {
		return new ConstantResult(unk);
	}

	@Override
	public EvalResult visitPValueList(PValueList<?> list, Void param) {
		return new ConstantResult(list);
	}

	@Override
	public EvalResult visitCommandHandleState(CommandHandleState state, Void param) {
		return new ConstantResult(state);
	}

	@Override
	public EvalResult visitNodeFailure(NodeFailureType type, Void param) {
		return new ConstantResult(type);
	}

	@Override
	public EvalResult visitNodeOutcome(NodeOutcome outcome, Void param) {
		return new ConstantResult(outcome);
	}

	@Override
	public EvalResult visitNodeState(NodeState state, Void param) {
		return new ConstantResult(state);
	}

	
}