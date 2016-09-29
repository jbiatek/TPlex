package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public class IntegerValue implements PInteger {
	
	private final int value;
	
	public static IntegerValue get(int v) {
		return new IntegerValue(v);
	}
	
	/**
	 * Wrap this value in PLEXIL. Please only use this if you're doing 
	 * something unusual such as symbolic execution, otherwise the factory
	 * method IntegerValue.get() is the canonical way to get a wrapped int.
	 * @param v
	 */
	public IntegerValue(int v) {
		value = v;
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
	public PReal castToReal() {
		return RealValue.get((double) value);
	}

	@Override
	public int getIntValue() {
		return value;
	}

	@Override
	public PBoolean gt(PInteger o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		} else {
			return BooleanValue.get(value > o.getIntValue());
		}
	}

	@Override
	public PBoolean ge(PInteger o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		} else {
			return BooleanValue.get(value >= o.getIntValue());
		}
	}

	@Override
	public PBoolean lt(PInteger o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		} else {
			return BooleanValue.get(value < o.getIntValue());
		}
	}

	@Override
	public PBoolean le(PInteger o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		} else {
			return BooleanValue.get(value <= o.getIntValue());
		}
	}

	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		if (o instanceof IntegerValue) {
			IntegerValue other = (IntegerValue) o;
			return BooleanValue.get(value == other.value);
		} else if (o instanceof RealValue) {
			//TODO: Find out if this is correct, or if a delta is used, or what
			RealValue other = (RealValue) o;
			return BooleanValue.get(value == other.getRealValue());
		}
		return BooleanValue.get(false);
	}

	@Override
	public PInteger add(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(value + o.getIntValue());
		}
	}

	@Override
	public PInteger sub(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get( value - o.getIntValue());
		}
	}

	@Override
	public PInteger mul(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(value * o.getIntValue());
		}
	}

	@Override
	public PInteger div(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(value / o.getIntValue());
		}
	}

	@Override
	public PInteger mod(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(value % o.getIntValue());
		}
	}

	@Override
	public PInteger max(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(Math.max(value, o.getIntValue()));
		}
	}

	@Override
	public PInteger min(PInteger o) {
		if (o.isUnknown()) {
			return UnknownInt.get();
		} else {
			return IntegerValue.get(Math.min(value, o.getIntValue()));
		}
	}

	@Override
	public PInteger abs() {
		return IntegerValue.get(Math.abs(value));
	}

	@Override
	public ILType getType() {
		return ILType.INTEGER;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PInteger) {
			PInteger other = (PInteger) o;
			return other.isKnown() && this.value == other.getIntValue();
		} 
		return false;
	}
	
	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public String toString() {
		return value+"";
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.INTEGER;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}
	
}
