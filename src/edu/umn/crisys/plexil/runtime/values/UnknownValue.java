package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public class UnknownValue implements PBoolean, PInteger, PReal, PString {

	private static UnknownValue singleton = new UnknownValue();
    public static boolean SINGLETON = true;
	
	private UnknownValue() {}
	
	public static UnknownValue get() {
	    if (!SINGLETON) {
	        return new UnknownValue();
	    }
		return singleton;
	}

	@Override
	public String toString() {
		return "UNKNOWN";
	}
	
	@Override
	public boolean isKnown() {
		return false;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public int getIntValue() {
		throw new RuntimeException("Tried to use the value of UNKNOWN");
	}

	@Override
	public double getRealValue() {
		throw new RuntimeException("Tried to use the value of UNKNOWN");
	}

	@Override
	public String getString() {
		throw new RuntimeException("Tried to use the value of UNKNOWN");
	}

	@Override
	public PBoolean equalTo(PValue o) {
		return this;
	}

	@Override
	public ILType getType() {
		return ILType.UNKNOWN;
	}

	@Override
	public PValue castTo(ILType type) {
		return type.getUnknown();
	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public boolean isFalse() {
		return false;
	}

	@Override
    public boolean isNotFalse() {
        return true;
    }

    @Override
    public boolean isNotTrue() {
        return true;
    }

    @Override
	public PBoolean or(PBoolean o) {
		return o.isTrue() ? o : this;
	}

	@Override
	public PBoolean xor(PBoolean o) {
		return this;
	}

	@Override
	public PBoolean and(PBoolean o) {
		return o.isFalse()? o : this;
	}

	@Override
	public PBoolean not() {
		return this;
	}

	@Override
	public boolean isReal() {
		return false;
	}

	@Override
	public PInteger castToInteger() {
		return this;
	}

	@Override
	public PReal castToReal() {
		return this;
	}

	@Override
	public PBoolean gt(PNumeric o) {
		return this;
	}

	@Override
	public PBoolean ge(PNumeric o) {
		return this;
	}

	@Override
	public PBoolean lt(PNumeric o) {
		return this;
	}

	@Override
	public PBoolean le(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric add(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric sub(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric mul(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric div(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric mod(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric max(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric min(PNumeric o) {
		return this;
	}

	@Override
	public PNumeric sqrt() {
		return this;
	}

	@Override
	public PNumeric abs() {
		return this;
	}

	@Override
	public PString concat(PString o) {
		return this;
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
		return PlexilType.UNKNOWN;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

}
