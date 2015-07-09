package edu.umn.crisys.plexil.expr.il.nativebool;

import java.util.Optional;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.runtime.values.PValue;

/**
 * Expression representing a native "equal" method. For Java, this means 
 * the equals() method. For Lustre, this means the "=" operator. 
 */
public class NativeEqual implements NativeExpr {
	
	private Expression left;
	private Expression right;

	public NativeEqual(Expression left, Expression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException("These should be the same type, but they are "
					+left.getType()+" and "+right.getType());
		}
		this.left = left;
		this.right = right;
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	@Override
	public String toString() {
		return left + " equals " + right;
	}
	
	@Override
	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param) {
		return visitor.visitNativeEqual(this, param);
	}

	@Override
	public Optional<Boolean> eval() {
		Optional<PValue> leftEval = left.eval();
		Optional<PValue> rightEval = right.eval();
		
		if (leftEval.isPresent() && rightEval.isPresent()) {
			return Optional.of(leftEval.get().equals(rightEval.get()));
		}
		return Optional.empty();
	}

}
