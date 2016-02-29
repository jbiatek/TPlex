package edu.umn.crisys.plexil.expr.ast;

import edu.umn.crisys.plexil.il.expr.ILType;

public enum PlexilType {

	BOOLEAN,
	INTEGER,
	REAL,
	STRING,
	STATE,
	OUTCOME,
	FAILURE,
	COMMAND_HANDLE,
	NODEREF,
	UNKNOWN,
	ARRAY,
	BOOLEAN_ARRAY,
	INTEGER_ARRAY,
	REAL_ARRAY,
	STRING_ARRAY
	;
	
    /**
     * For array types, get the element type.
     * @return
     */
    public PlexilType elementType() {
        switch (this) {
        case BOOLEAN_ARRAY:
            return BOOLEAN;
        case INTEGER_ARRAY:
            return INTEGER;
        case REAL_ARRAY:
            return REAL;
        case STRING_ARRAY:
            return STRING;
        case ARRAY:
            return UNKNOWN;
        default:
        	throw new RuntimeException(this+" is not an array and has no elements");
        }
    }
    
    public ILType toILType() {
    	switch(this) {
		case ARRAY: return ILType.ARRAY;
		case BOOLEAN: return ILType.BOOLEAN;
		case BOOLEAN_ARRAY: return ILType.BOOLEAN_ARRAY;
		case COMMAND_HANDLE: return ILType.COMMAND_HANDLE;
		case FAILURE: return ILType.FAILURE;
		case INTEGER: return ILType.INTEGER;
		case INTEGER_ARRAY: return ILType.INTEGER_ARRAY;
		case NODEREF: return ILType.NODEREF;
		case OUTCOME: return ILType.OUTCOME;
		case REAL: return ILType.REAL;
		case REAL_ARRAY: return ILType.REAL_ARRAY;
		case STATE: return ILType.STATE;
		case STRING: return ILType.STRING;
		case STRING_ARRAY: return ILType.STRING_ARRAY;
		case UNKNOWN: return ILType.UNKNOWN;
		default:
			throw new RuntimeException("Missing case: "+this);
    	}
    }
    
    /**
     * Get the array type that contains this type as elements.
     * @return
     */
    public PlexilType toArrayType() {
        switch (this) {
        case BOOLEAN:
            return BOOLEAN_ARRAY;
        case INTEGER:
            return INTEGER_ARRAY;
        case REAL:
            return REAL_ARRAY;
        case STRING:
            return STRING_ARRAY;
        default:
        	throw new RuntimeException("Cannot make an array of "+this);
        }
    }

    
    /**
     * Returns true if this type represents an array.
     * @return
     */
    public boolean isArrayType() {
        return this.toString().contains("ARRAY");
    }
    
    /**
     * @return true if this type is implemented using an enum.
     */
    public boolean isEnumeratedType() {
        switch (this) {
        case STATE:
        case OUTCOME:
        case FAILURE:
        case COMMAND_HANDLE:
            return true;
        default:
        	return false;
        }
    }

    /**
     * @return true if this is a numeric type of some sort.
     */
    public boolean isNumeric() {
        return this == INTEGER || this == REAL;// || this == NUMERIC;
    }
	
    public boolean isSpecificType() {
    	return /*this != NUMERIC && */this != UNKNOWN &&  this != ARRAY;
    }

    /**
     * Ensure that the given PlexilType is not incompatible with this one.
     * (note that we didn't say "compatible".) This is pretty permissive about
     * types. If either of these is UNKNOWN, we just let it go. Otherwise,
     * if both are numeric, everything is fine, and pretty much everything else
     * has to be an exact match. 
     * @param other
     */
    public void typeCheck(PlexilType other) {
        // Plexil has a lot of unknown types, like lookups, so we'll be 
        // permissive. Ideally, we might require lookups to have declared
        // types (this is now built in to the spec). 
        if (this == UNKNOWN || other == UNKNOWN) return;

        if (this.isNumeric()) {
            if (other.isNumeric()) {
                return;
            }
            throw new RuntimeException("Type error: Trying to use "+other+" as a "+this);
        }
        else if (this == ARRAY) {
            if (other.isArrayType()) return;
        }
        
        // They better just be the same at this point.
        if (this != other) {
            throw new RuntimeException("Type error: Trying to use "+other+" as a "+this);
        }        
    }

    
    public PlexilType getMoreSpecific(PlexilType other) {
    	if (this.isSpecificType() && !other.isSpecificType()) {
    		this.typeCheck(other);
    		return this;
    	} else if (other.isSpecificType() && !this.isSpecificType()) {
    		other.typeCheck(this);
    		return other;
    	} else if (other == UNKNOWN) {
    		return this;
    	} else if (this == UNKNOWN) {
    		return other;
    	} else if ( (this == REAL && other.isNumeric())
    			|| (other == REAL && this.isNumeric())) {
    		return REAL;
    	}
    	if (this != other) {
    		throw new RuntimeException("Trying to compare "+this+" and "+other);
    	}
    	// They are equal
    	return this;
    }

}
