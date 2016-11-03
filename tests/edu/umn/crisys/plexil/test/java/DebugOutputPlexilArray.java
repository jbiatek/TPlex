package edu.umn.crisys.plexil.test.java;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprBase;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

/**
 * When we read PLEXIL debug output logs, we get basically a value dump straight
 * out of the C++ interpreter. Most of it is fine, but one slightly tricky area
 * is arrays. They are printed but without any useful type information. That's
 * where this class comes in. It stores the values that PLEXIL gives us as 
 * strings, not trying to parse them or cast them to a particular type. We can
 * then perform a "non strict" check for equality. For example, booleans are 
 * represented as 1 and 0,
 * but of course so are integers. This will ensure that 1 and true match each
 * other, and so on. 
 * 
 * @author jbiatek
 *
 */
public class DebugOutputPlexilArray extends ILExprBase implements PValue {

    private String[] values;
    
    public DebugOutputPlexilArray(String valStr) {
    	super(ILType.UNKNOWN);
        String justArray = valStr.replaceFirst("Array: \\[", "")
                                .replaceFirst("\\]$", "");
        values = justArray.split(", ");
        if (justArray.equals("")) {
            values = new String[] {};
        }
    }
    
    /**
     * Check for equality loosely, since this array represents data gathered
     * from PLEXIL debug output that isn't easy to know the type of. This
     * method does things like assume 1 means either the number 1 or the 
     * boolean true. 
     * 
     * @param o
     * @return
     */
    public boolean nonStrictEquals(ILExpr o) {
        if (o instanceof List<?>) {
            List<?> arr = (List<?>) o;
            if (arr.size() != values.length) {
                System.out.println(arr.size());
                System.out.println(values.length);
                return false;
            }
            
            for (int i=0; i<values.length; i++) {
            	Object valueObj = arr.get(i);
            	if (! (valueObj instanceof PValue )) {
            		return false;
            	}
                if (!checkIndividual(values[i], 
                        (PValue) arr.get(i))) {
                    return false;
                }
            }
            
            return true;
        } else {
        	return equals(o);
        }
        	
    }
    
    @Override
    public boolean equals(ILExpr e) {
    	if (e instanceof DebugOutputPlexilArray) {
            return Arrays.equals(values, ((DebugOutputPlexilArray) e).values);
        }
    	return false;
    }
    
	@Override
	public int hashCode() {
		return 0;
	}

    private boolean checkIndividual(String mine, PValue theirs) {
        if (mine.equals("<unknown>")) {
            return theirs.isUnknown();
        } else if ( theirs instanceof BooleanValue) {
            if (mine.equals("1")) {
                return ((BooleanValue) theirs).isTrue();
            } else {
                return ((BooleanValue) theirs).isFalse();
            }
        } else if (theirs instanceof IntegerValue) {
            return theirs.equals(IntegerValue.get(Integer.parseInt(mine)));
        } else if (theirs instanceof RealValue) {
            // the executive seems to do some rounding...
            double theirDouble = ((RealValue) theirs).getRealValue();
            return (Math.abs(theirDouble-Double.parseDouble(mine)) < .01);
        } else if (theirs instanceof StringValue) {
            return theirs.equals(StringValue.get(
                    mine.replaceAll("^\"", "").replaceAll("\"$", "")));
        }
        return false;
    }
    
    @Override
    public String asString() {
        return Arrays.toString(values);
    }

	@Override
	public PBoolean equalTo(PValue o) {
		return BooleanValue.get(equals(o));
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
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		throw new RuntimeException("Why are you visiting this? It's an internal testing class.");
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.UNKNOWN;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		throw new RuntimeException("Why are you visiting this? It's an internal testing class.");
	}


    
}
