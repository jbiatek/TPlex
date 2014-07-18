package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;

public class RealValue implements PReal {
	
	private final double value;
	
	public static RealValue get(double value) {
		return new RealValue(value);
	}
	
	private RealValue(double value) {
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
	public PValue castTo(PlexilType type) {
		if (type == PlexilType.INTEGER) {
			return castToInteger();
		}
		return PValue.Util.defaultCastTo(this, type);
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

	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitRealValue(this, param);
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public String asString() {
		return toString();
	}

	@Override
	public boolean isAssignable() {
		return false;
	}
}