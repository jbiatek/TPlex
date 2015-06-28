package edu.umn.crisys.plexil.expr.il.nativebool;

import edu.umn.crisys.plexil.expr.Expression;

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

}
