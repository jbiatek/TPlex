package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public class RealValue implements PReal {
	
	private final double value;
	
	public static RealValue get(double value) {
		return new RealValue(value);
	}
	
	/**
	 * Wrap this value in PLEXIL. Please only use this if you're doing 
	 * something unusual such as symbolic execution, otherwise the factory
	 * method RealValue.get() is the canonical way to get a wrapped double.
	 * @param value
	 */
	public RealValue(double value) {
		this.value = value;
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
	public double getRealValue() {
		return value;
	}

	@Override
	public PBoolean gt(PReal o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		return BooleanValue.get(value > o.getRealValue());
	}

	@Override
	public PBoolean ge(PReal o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		return BooleanValue.get(value >= o.getRealValue());
	}

	@Override
	public PBoolean lt(PReal o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		return BooleanValue.get(value < o.getRealValue());
	}

	@Override
	public PBoolean le(PReal o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		return BooleanValue.get(value <= o.getRealValue());
	}

	@Override
	public PReal add(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(value + o.getRealValue());
	}

	@Override
	public PReal sub(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get( value - o.getRealValue());
	}

	@Override
	public PReal mul(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(value * o.getRealValue());
	}

	@Override
	public PReal div(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(value / o.getRealValue());
	}

	@Override
	public PReal mod(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(value % o.getRealValue());
	}

	@Override
	public PReal max(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(Math.max(value, o.getRealValue()));
	}

	@Override
	public PReal min(PReal o) {
		if (o.isUnknown()) {
			return UnknownReal.get();
		}
		return RealValue.get(Math.max(value, o.getRealValue()));
	}

	@Override
	public PReal sqrt() {
		return RealValue.get(Math.sqrt(value));
	}

	@Override
	public PReal abs() {
		return RealValue.get(Math.abs(value));
	}

	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		if (o instanceof PReal) {
			PReal other = (PReal) o;
			return BooleanValue.get(value == other.getRealValue());
		}
		return BooleanValue.get(false);
	}

	@Override
	public ILType getType() {
		return ILType.REAL;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PReal) {
			PReal other = (PReal) o;
			return other.isKnown() && this.value == other.getRealValue();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int) value;
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
		return PlexilType.REAL;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	private boolean outOfIntRange() {
		return value > Integer.MAX_VALUE || value < Integer.MIN_VALUE;
	}
	
	@Override
	public PInteger ceil() {
		if (outOfIntRange()) {
			return UnknownInt.get();
		}
		return IntegerValue.get((int)Math.ceil(value));
	}

	@Override
	public PInteger floor() {
		if (outOfIntRange()) {
			return UnknownInt.get();
		}
		return IntegerValue.get((int)Math.floor(value));
	}

	@Override
	public PInteger round() {
		if (outOfIntRange()) {
			return UnknownInt.get();
		}
		return IntegerValue.get((int)Math.round(value));
	}

	@Override
	public PInteger trunc() {
		if (outOfIntRange()) {
			return UnknownInt.get();
		}
		return value < 0 ? ceil() : floor();
	}

	@Override
	public PInteger real_to_int() {
		if (outOfIntRange()) {
			return UnknownInt.get();
		}
		return value - (int)value == 0 ? 
				IntegerValue.get((int)value)
				: UnknownInt.get();
	}

}
