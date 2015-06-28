package edu.umn.crisys.plexil.expr.il.nativebool;

public class NativeConstant implements NativeExpr {

	public static final NativeConstant TRUE = new NativeConstant(true);
	public static final NativeConstant FALSE = new NativeConstant(false);
	private boolean value;
	
	private NativeConstant(boolean value) {
		this.value = value;
	}
	
	@Override
	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param) {
		return visitor.visitNativeConstant(this, param);
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "True" : "False";
	}
}
