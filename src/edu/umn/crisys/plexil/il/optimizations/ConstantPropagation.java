package edu.umn.crisys.plexil.il.optimizations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.il.ILExprModifier;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
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
	public Expression visit(Expression e, Void param) {
		Optional<PValue> eval = e.eval();
		if (eval.isPresent()) {
			return eval.get();
		} else {
			return visitComposite(e, param);
		}
	}
	
//	@Override
//	public Expression visit(NamedCondition named, Void param) {
//		Expression optimized = visit((Expression) named, param);
//		if (optimized instanceof NamedCondition) {
//			// If it's not an operator, just use it directly instead. 
//			NamedCondition newNamed = (NamedCondition) optimized;
//			if ( ! (newNamed.getExpression() instanceof ILOperation)) {
//				return newNamed.getExpression();
//			}
//		}
//		return optimized;
//	}

	@Override
	public Expression visit(ILOperation op, Void param) {
		Optional<PValue> eval = op.eval();
		if (eval.isPresent()) {
			// This whole thing is constant! 
			return eval.get();
		}
		
		// Well, bits of it might be redundant. 
		final PValue identity;
		switch(op.getOperator()) {
		case PAND: identity = BooleanValue.get(true); break;
		case POR: identity = BooleanValue.get(false); break;
		case AND: identity = NativeBool.TRUE; break;
		case OR: identity = NativeBool.FALSE; break;
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
				// Whatever is left should propagate constants too
				.map((arg) -> arg.accept(this))
				.collect(Collectors.toList());
		if (newArgs.size() == 1) {
			// No operation necessary. Just use the last remaining 
			// subexpression.
			return newArgs.get(0);
		}
		
		return op.getCloneWithArgs(newArgs);
	}

}
