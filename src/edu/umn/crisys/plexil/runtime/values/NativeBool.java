package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.expr.il.ExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILType;

public enum NativeBool implements PValue {

	TRUE,
	FALSE;
	
	public static NativeBool wrap(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	public boolean getValue() {
		return this == TRUE;
	}
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return this.toString();
	}

	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public boolean isUnknown() {
		return false;
	}

	@Override
	public ILType getType() {
		return ILType.NATIVE_BOOL;
	}

}
