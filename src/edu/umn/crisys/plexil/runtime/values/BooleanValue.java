package edu.umn.crisys.plexil.runtime.values;


import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprSingletonBase;
import edu.umn.crisys.plexil.il.expr.ILType;

/**
 * Implements a known boolean value.
 * 
 * @author Jason Biatek
 *
 */
public class BooleanValue extends ILExprSingletonBase implements PBoolean {
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
	 * Create a known BooleanValue with the given value. Unless you're doing 
	 * unusual things, such as using symbolic values, you'll probably get 
	 * better performance using BooleanValue.get() rather than creating a bunch
	 * of identical objects. 
	 * 
	 * @param value The value to wrap
	 */
	public BooleanValue(boolean value) {
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
			return UnknownBool.get();
		}
		else if ( ! (o instanceof BooleanValue)) {
			return singleFalse;
		}
		return this == o ? singleTrue : singleFalse;
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
	public int hashCode() {
		return bool? 1 : 0;
	}
	
	@Override
	public boolean equals(ILExpr e) {
	    if (e instanceof BooleanValue) {
	        return ((BooleanValue) e).bool == this.bool;
	    } 
	    return false;
	}
	
	
	@Override
	public String asString() {
		return bool+"";
	}
	
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.BOOLEAN;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	@Override
	public ILType getType() {
		return ILType.BOOLEAN;
	}

}
