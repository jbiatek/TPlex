package edu.umn.crisys.plexil.il.expr.nativebool;

public interface NativeExpr {

	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param);
	
}
