package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;

/**
 * Implements a known boolean value.
 * 
 * @author Jason Biatek
 *
 */
public class BooleanValue implements PBoolean {
	public static boolean SINGLETON = true;

    /**
	 * The actual boolean.
	 */
	private final boolean bool;
	
	private static final BooleanValue singleFalse = new BooleanValue(false);
	private static final BooleanValue singleTrue = new BooleanValue(true);
	
	public static BooleanValue get(boolean val) {
	    if (!SINGLETON) {
	        return new BooleanValue(val);
	    }
		if (val) {
			return singleTrue;
		}
		return singleFalse;
	}
	
	/**
	 * Create a known BooleanValue with the given value.
	 * @param value
	 */
	private BooleanValue(boolean value) {
		this.bool = value;
	}
	
	@Override
	public boolean isTrue() {
		return bool;
	}
	
	@Override
	public boolean isFalse() {
		return !bool;
	}
	
	@Override
    public boolean isNotFalse() {
        return bool;
    }

    @Override
    public boolean isNotTrue() {
        return !bool;
    }

    @Override
	public PBoolean or(PBoolean o) {
		// T || U == T
		// F || U == U
		// U || U == U
		
		// If I'm true, the result is true, if I'm false, I go away:
		return bool ? this : o;
		
	}
	
	@Override
	public PBoolean xor(PBoolean o) {
		// T, F, U XOR U == U
		if (o.isKnown()) {
			return bool ^ o.isTrue() ? singleTrue : singleFalse;
		} else {
			return o;
		}
	}
	
	@Override
	public PBoolean and(PBoolean o) {
		// T && U == U
		// F && U == F
		// U && U == U

		// If I'm true, it depends on o, if I'm false, the answer is false
		return bool ? o : this;
	}
	
	@Override
	public BooleanValue not() {
		return bool ? singleFalse : singleTrue;
	}
	
	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		else if ( ! (o instanceof BooleanValue)) {
			return singleFalse;
		}
		return this == o ? singleTrue : singleFalse;
	}
	
	@Override
	public PlexilType getType() {
		return PlexilType.BOOLEAN;
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
		return PValue.Util.defaultCastTo(this, type);
	}
	
	@Override
	public int hashCode() {
		return bool? 1 : 0;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o instanceof BooleanValue) {
	        return o == this;
	    } else if (o instanceof Boolean) {
	        return ((Boolean) bool).equals(o);
	    }
	    return false;
	}
	
	
	@Override
	public String toString() {
		return bool+"";
	}

	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitBooleanValue(this, param);
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