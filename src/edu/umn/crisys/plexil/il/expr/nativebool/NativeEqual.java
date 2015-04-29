package edu.umn.crisys.plexil.il.expr.nativebool;

import edu.umn.crisys.plexil.ast.expr.ILExpression;

/**
 * Expression representing a native "equal" method. For Java, this means 
 * the equals() method. For Lustre, this means the "=" operator. 
 */
public class NativeEqual implements NativeExpr {
	
	private ILExpression left;
	private ILExpression right;

	public NativeEqual(ILExpression left, ILExpression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException("These should be the same type, but they are "
					+left.getType()+" and "+right.getType());
		}
		this.left = left;
		this.right = right;
	}

	public ILExpression getLeft() {
		return left;
	}

	public ILExpression getRight() {
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
