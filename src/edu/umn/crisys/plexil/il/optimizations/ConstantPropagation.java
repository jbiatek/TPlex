package edu.umn.crisys.plexil.il.optimizations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.ILOperation;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class ConstantPropagation extends ILExprModifier<Void> {
	
	/**
	 * Consolidate and remove expressions or parts of expressions which are
	 * constant. 
	 * 
	 * @param ilPlan
	 */
	public static void optimize(Plan ilPlan) {
		ilPlan.modifyAllGuards(new ConstantPropagation(), null);
	}

	@Override
	public ILExpr visit(ILExpr e, Void param) {
		// Replace any constants found.
		Optional<PValue> eval = e.eval();
		if (eval.isPresent()) {
			return eval.get();
		} else {
			return visitComposite(e, param);
		}
	}
	
	@Override
	public ILExpr visit(ILOperation op, Void param) {
		Optional<PValue> eval = op.eval();
		if (eval.isPresent()) {
			// This whole thing is constant! 
			return eval.get();
		}
		
		// Well, bits of it might be redundant. Things like true && something.
		if ( ! op.getOperator().getIdentityValue().isPresent()) {
			// Okay, nothing special to do, other than propagate the listener,
			// which is done by default. 
			return super.visit(op, param);
		}
		final PValue identity = op.getOperator().getIdentityValue().get();
		
		// Remove identity expressions if found. 
		List<ILExpr> newArgs = op.getArguments().stream()
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
