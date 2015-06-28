package edu.umn.crisys.plexil.expr.il.nativebool;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.il.ILEval;
import edu.umn.crisys.plexil.expr.il.nativebool.NativeOperation.NativeOp;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class NativeEval implements NativeExprVisitor<Void, Optional<Boolean>> {

	@Override
	public Optional<Boolean> visitNativeOperation(NativeOperation op, Void param) {
		if (op.getOperation() == NativeOp.NOT) {
			return op.getArgs().get(0).accept(this, param)
					.map((v) -> (!v));
		}
			
		boolean shortCircuiter = op.getOperation() == NativeOp.AND ? false : true;
		List<Optional<Boolean>> argResults = op.getArgs().stream()
				.map((arg) -> arg.accept(this, null))
				.collect(Collectors.toList());
		if (argResults.stream().anyMatch((res) -> res.isPresent() && res.get() == shortCircuiter)) {
			// Something is definitely the short circuit
			return Optional.of(shortCircuiter);
		} else if (argResults.stream().anyMatch((res) -> ! res.isPresent())) {
			// Something isn't constant
			return Optional.empty();
		} else {
			// Must all be constantly the other thing
			return Optional.of(! shortCircuiter);
		}
	}

	@Override
	public Optional<Boolean> visitPlexilExprToNative(PlexilExprToNative pen,
			Void param) {
		return pen.getPlexilExpr().accept(new ILEval(), null)
				.map((v) -> pen.getCondition().checkValue(v));
	}

	@Override
	public Optional<Boolean> visitNativeConstant(NativeConstant c, Void param) {
		return Optional.of(c.getValue());
	}

	@Override
	public Optional<Boolean> visitNativeEqual(NativeEqual e, Void param) {
		Optional<PValue> left = e.getLeft().accept(new ILEval(), null);
		Optional<PValue> right = e.getRight().accept(new ILEval(), null);
		
		if (left.isPresent() && right.isPresent()) {
			return Optional.of(left.get().equals(right.get()));
		} else {
			return Optional.empty();
		}
	}

}
