package edu.umn.crisys.plexil.expr.il.nativebool;

public interface NativeExpr {

	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param);
	
}
