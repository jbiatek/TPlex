package edu.umn.crisys.plexil.il.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.ast.expr.Expression;
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
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILEval extends ILExprVisitor<Void, Optional<PValue>> {
	
	/**
	 * If this expression is a clause in a larger `op` operation, this method
	 * returns true if the clause can simply be deleted without changing the
	 * result of the equation (for example, in "false || foo", the "false" clause
	 * can be dropped.)
	 * @param op
	 * @param expr
	 * @return
	 */
    public static boolean clauseIsSkippable(Operator op, Expression expr) {
    	if ((op == Operator.AND || op == Operator.OR)) {
    		Optional<PValue> result = expr.accept(new ILEval(), null);
    		if (result.isPresent()) {
    			PBoolean theValue = (PBoolean) result.get();
	    		// Maybe. It's a constant, and we're using a known operator.
	    		if (op == Operator.AND && theValue.isTrue()
	    				|| op == Operator.OR && theValue.isFalse()) {
	    			return true;
	    		}
    		}
    	}
    	return false;
    }

	
	@Override
	public Optional<PValue> visitArrayIndex(ArrayIndexExpr array, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitLookupNow(LookupNowExpr lookup, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitLookupOnChange(LookupOnChangeExpr lookup, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitOperation(Operation op, Void param) {
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
			Optional<PValue> argResult = arg.accept(this, null);
			if (argResult.isPresent()) {
				if (shortCircuiter != null && argResult.get().equals(shortCircuiter)) {
					// We have short circuited! This is always going to be it.
					return argResult;
				} else {
					// Just a regular constant. 
					values.add(argResult.get());
				}
			} else {
				// Not constant. Could be anything.
				return Optional.empty();
			}
		}
		// All the arguments were constant, so this should be constant too. 
		return Optional.of(op.getOperator().eval(values));
	}
	
	@Override
	public Optional<PValue> visitRootParentState(RootParentStateExpr state, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitRootParentExit(RootAncestorExitExpr ancExit,
			Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitRootParentEnd(RootAncestorEndExpr ancEnd, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, Void param) {
		return Optional.empty();
	}


	@Override
	public Optional<PValue> visitSimple(SimpleVar var, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitArray(ArrayVar array, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitLibrary(LibraryVar lib, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitAlias(AliasExpr alias, Void param) {
		return Optional.empty();
	}

	@Override
	public Optional<PValue> visitGetNodeState(GetNodeStateExpr state, Void param) {
		return Optional.empty();
	}
	
	@Override
	public Optional<PValue> visitBooleanValue(BooleanValue bool, Void param) {
		return Optional.of(bool);
	}

	@Override
	public Optional<PValue> visitIntegerValue(IntegerValue integer, Void param) {
		return Optional.of(integer);
	}

	@Override
	public Optional<PValue> visitRealValue(RealValue real, Void param) {
		return Optional.of(real);
	}

	@Override
	public Optional<PValue> visitStringValue(StringValue string, Void param) {
		return Optional.of(string);
	}

	@Override
	public Optional<PValue> visitUnknownValue(UnknownValue unk, Void param) {
		return Optional.of(unk);
	}

	@Override
	public Optional<PValue> visitPValueList(PValueList<?> list, Void param) {
		return Optional.of(list);
	}

	@Override
	public Optional<PValue> visitCommandHandleState(CommandHandleState state, Void param) {
		return Optional.of(state);
	}

	@Override
	public Optional<PValue> visitNodeFailure(NodeFailureType type, Void param) {
		return Optional.of(type);
	}

	@Override
	public Optional<PValue> visitNodeOutcome(NodeOutcome outcome, Void param) {
		return Optional.of(outcome);
	}

	@Override
	public Optional<PValue> visitNodeState(NodeState state, Void param) {
		return Optional.of(state);
	}

	
}