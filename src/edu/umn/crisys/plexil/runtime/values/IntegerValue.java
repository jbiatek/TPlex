package edu.umn.crisys.plexil.runtime.values;

public class IntegerValue implements PInteger {
	
	private final int value;
	
	public static IntegerValue get(int v) {
		return new IntegerValue(v);
	}
	
	private IntegerValue(int v) {
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
	public boolean isReal() {
		// Nope, not real.
		return false;
	}

	@Override
	public IntegerValue castToInteger() {
		// already an int
		return this;
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
	public double getRealValue() {
		return (double) value;
	}

	@Override
	public PBoolean gt(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().gt(o);
		} else {
			return BooleanValue.get(value > o.getIntValue());
		}
	}

	@Override
	public PBoolean ge(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().ge(o);
		} else {
			return BooleanValue.get(value >= o.getIntValue());
		}
	}

	@Override
	public PBoolean lt(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().lt(o);
		} else {
			return BooleanValue.get(value < o.getIntValue());
		}
	}

	@Override
	public PBoolean le(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().le(o);
		} else {
			return BooleanValue.get(value <= o.getIntValue());
		}
	}

	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
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
	public PNumeric add(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().add(o);
		} else {
			return IntegerValue.get(value + o.getIntValue());
		}
	}

	@Override
	public PNumeric sub(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().sub(o);
		} else {
			return IntegerValue.get( value - o.getIntValue());
		}
	}

	@Override
	public PNumeric mul(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().mul(o);
		} else {
			return IntegerValue.get(value * o.getIntValue());
		}
	}

	@Override
	public PNumeric div(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().div(o);
		} else {
			return IntegerValue.get(value / o.getIntValue());
		}
	}

	@Override
	public PNumeric mod(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().mod(o);
		} else {
			return IntegerValue.get(value % o.getIntValue());
		}
	}

	@Override
	public PNumeric max(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().max(o);
		} else {
			return IntegerValue.get(Math.max(value, o.getIntValue()));
		}
	}

	@Override
	public PNumeric min(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o.isReal()) {
			return castToReal().min(o);
		} else {
			return IntegerValue.get(Math.min(value, o.getIntValue()));
		}
	}

	@Override
	public PNumeric sqrt() {
		return RealValue.get(Math.sqrt(value));
	}

	@Override
	public PNumeric abs() {
		return IntegerValue.get(Math.abs(value));
	}

	@Override
	public PlexilType getType() {
		return PlexilType.INTEGER;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PNumeric) {
			PNumeric other = (PNumeric) o;
			return other.isKnown() && this.value == other.getRealValue();
		} else if (o instanceof Integer) {
		    return ((Integer) value).equals(o);
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
	public <P, R> R accept(PValueVisitor<P, R> visitor, P param) {
		return visitor.visitIntegerValue(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}
	
}
