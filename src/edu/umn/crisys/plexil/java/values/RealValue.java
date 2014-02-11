package edu.umn.crisys.plexil.java.values;

public class RealValue extends StandardValue implements PReal {
	
	private final double value;
	
	public static RealValue get(double value) {
		return new RealValue(value);
	}
	
	private RealValue(double value) {
		this.value = value;
	}
	
	@Override
	public PValue castTo(PlexilType type) {
		if (type == PlexilType.INTEGER) {
			return castToInteger();
		}
		return super.castTo(type);
	}

	@Override
	public Object asNativeJava() {
	    return value;
	}


	@Override
	public boolean isReal() {
		return true; // yes, this is real
	}

	@Override
	public PInteger castToInteger() {
		return IntegerValue.get((int) value);
	}

	@Override
	public PReal castToReal() {
		return this; // already a real;
	}

	@Override
	public int getIntValue() {
		return (int) value;
	}

	@Override
	public double getRealValue() {
		return value;
	}

	@Override
	public PBoolean gt(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return BooleanValue.get(value > o.getRealValue());
	}

	@Override
	public PBoolean ge(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return BooleanValue.get(value >= o.getRealValue());
	}

	@Override
	public PBoolean lt(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return BooleanValue.get(value < o.getRealValue());
	}

	@Override
	public PBoolean le(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return BooleanValue.get(value <= o.getRealValue());
	}

	@Override
	public PNumeric add(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(value + o.getRealValue());
	}

	@Override
	public PNumeric sub(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get( value - o.getRealValue());
	}

	@Override
	public PNumeric mul(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(value * o.getRealValue());
	}

	@Override
	public PNumeric div(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(value / o.getRealValue());
	}

	@Override
	public PNumeric mod(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(value % o.getRealValue());
	}

	@Override
	public PNumeric max(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(Math.max(value, o.getRealValue()));
	}

	@Override
	public PNumeric min(PNumeric o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return RealValue.get(Math.max(value, o.getRealValue()));
	}

	@Override
	public PNumeric sqrt() {
		return RealValue.get(Math.sqrt(value));
	}

	@Override
	public PNumeric abs() {
		return RealValue.get(Math.abs(value));
	}

	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (o instanceof PNumeric) {
			PNumeric other = (PNumeric) o;
			return BooleanValue.get(value == other.getRealValue());
		}
		return BooleanValue.get(false);
	}

	@Override
	public PlexilType getType() {
		return PlexilType.REAL;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PNumeric) {
			PNumeric other = (PNumeric) o;
			return other.isKnown() && this.value == other.getRealValue();
		} else if (o instanceof Double) {
		    return ((Double) value).equals(o);
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
}
