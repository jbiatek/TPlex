package edu.umn.crisys.plexil.expr.il.nativebool;

import java.util.Optional;

public interface NativeExpr {

	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param);

	public Optional<Boolean> eval();
}
