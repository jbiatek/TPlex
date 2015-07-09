package edu.umn.crisys.plexil.il.optimizations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.common.Operation;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeConstant;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeExpr;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation;
import edu.umn.crisys.plexil.expr.il.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation.NativeOp;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class ConstantPropagation extends ILExprModifier<Void> {
	
	/**
	 * Consolidate and remove expressions or parts of expressions which are
	 * constant. 
	 * 
	 * @param ilPlan
	 */
	public static void optimize(Plan ilPlan) {
		ilPlan.modifyAllExpressions(new ConstantPropagation(), null);
	}

	@Override
	public NativeExpr visitNativeOperation(NativeOperation op, Void param) {
		Optional<Boolean> eval = op.eval();
		if (eval.isPresent()) {
			// This whole thing is constant. 
			return eval.get() ? NativeConstant.TRUE : NativeConstant.FALSE;
		}
		// Propagate, then possibly filter out some bits
		op.setArgs(op.getArgs().stream()
				.map((arg) -> arg.accept(this, null))
				.collect(Collectors.toList()));
		if (op.getOperation() == NativeOp.AND) {
			op.getArgs().removeIf(
					// Remove if it's definitely true, leave it otherwise
					(arg) -> arg.eval().orElse(false));
		} else if (op.getOperation() == NativeOp.OR) {
			op.getArgs().removeIf(
					(arg) -> arg.eval().map((b) -> !b)
					.orElse(false));
		}
		return op;
	}

	@Override
	public NativeExpr visitPlexilExprToNative(PlexilExprToNative pen, Void param) {
		Optional<PValue> eval = pen.getPlexilExpr().eval();
		if (eval.isPresent()) {
			return pen.getCondition().checkValue(eval.get()) ? 
					NativeConstant.TRUE : NativeConstant.FALSE;
		} else {
			return pen;
		}
	}
	
	
	@Override
	public Expression visit(Operation op, Void param) {
		Optional<PValue> eval = op.eval();
		if (eval.isPresent()) {
			// This whole thing is constant! 
			return eval.get();
		}
		
		// Well, bits of it might be redundant. 
		final PValue identity;
		switch(op.getOperator()) {
		case AND: identity = BooleanValue.get(true); break;
		case OR: identity = BooleanValue.get(false); break;
		case ADD: 
		case SUB: identity = IntegerValue.get(0); break;
		case MUL:
		case DIV: identity = IntegerValue.get(1); break;
		default: identity = null;
		}
		
		if (identity == null) {
			// Okay, nothing special to do, other than propagate the listener,
			// which is done by default. 
			return super.visit(op, param);
		}
		
		// Remove identity expressions if found. 
		List<Expression> newArgs = op.getArguments().stream()
				.filter((arg) -> {
					Optional<PValue> argEval = arg.eval();
					if (argEval.isPresent()) {
						// Keep it as long as we know it's not the identity.
						return argEval.get().equalTo(identity).isNotTrue();
					} else {
						// Not constant, keep it
						return true;
					}
				})
				// They should propagate constants too
				.map((arg) -> arg.accept(this, null))
				.collect(Collectors.toList());
		return op.getCloneWithArgs(newArgs);
	}

}
