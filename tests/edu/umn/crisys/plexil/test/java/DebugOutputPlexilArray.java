package edu.umn.crisys.plexil.test.java;

import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;

public class DebugOutputPlexilArray implements PValue {

    private String[] values;
    
    public DebugOutputPlexilArray(String valStr) {
        String justArray = valStr.replaceFirst("Array: \\[", "")
                                .replaceFirst("\\]$", "");
        values = justArray.split(", ");
        if (justArray.equals("")) {
            values = new String[] {};
        }
    }
    
    public boolean equals(Object o) {
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
        } else if (o instanceof DebugOutputPlexilArray) {
            return Arrays.equals(values, ((DebugOutputPlexilArray) o).values);
        }
        return false;
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
    
    public String toString() {
        return Arrays.toString(values);
    }

	@Override
	public PBoolean equalTo(PValue o) {
		return BooleanValue.get(equals(o));
	}

	@Override
	public PlexilType getType() {
		return PlexilType.UNKNOWN;
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
	public String asString() {
		return toString();
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		throw new RuntimeException("Why are you visiting this? It's an internal testing class.");
	}
    
}
