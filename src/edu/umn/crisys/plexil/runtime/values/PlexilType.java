package edu.umn.crisys.plexil.runtime.values;

import java.util.Optional;

public enum PlexilType {

    BOOLEAN			(Optional.of(UnknownValue.get())),
    INTEGER			(Optional.of(UnknownValue.get())),
    REAL			(Optional.of(UnknownValue.get())),
    STRING			(Optional.of(UnknownValue.get())),
    NUMERIC			(Optional.of(UnknownValue.get())),
    UNKNOWN			(Optional.of(UnknownValue.get())),
    ARRAY           (Optional.empty()),
    BOOLEAN_ARRAY	(Optional.empty()),
    INTEGER_ARRAY   (Optional.empty()),
    REAL_ARRAY      (Optional.empty()),
    STRING_ARRAY    (Optional.empty()),
    STATE			(Optional.empty()),
    OUTCOME			(Optional.of(NodeOutcome.UNKNOWN)),
    FAILURE			(Optional.of(NodeFailureType.UNKNOWN)),
    COMMAND_HANDLE	(Optional.of(CommandHandleState.UNKNOWN)),
    NODEREF         (Optional.empty());
    // TODO: TIME ??????

    private final Optional<PValue> unknown;

    private PlexilType(Optional<PValue>unknown) {
    	
        this.unknown = unknown;
    }

    /**
     * Get an UNKNOWN for this type. Some types don't have an UNKNOWN, so be
     * careful.
     * @return
     */
    public PValue getUnknown() {
        return unknown.orElseThrow(() -> new RuntimeException(this+" does not have an UNKNOWN value"));
    }
    
    /**
     * Get the Class that represents this type. It may be an interface.
     */
    public Class<? extends PValue> getTypeClass() {
        switch(this) {
        case BOOLEAN: return PBoolean.class;
        case INTEGER: return PInteger.class;
        case REAL: return PReal.class;
        case STRING: return PString.class;
        case NUMERIC: return PNumeric.class;
        case STATE: return NodeState.class;
        case OUTCOME: return NodeOutcome.class;
        case FAILURE: return NodeFailureType.class;
        case COMMAND_HANDLE: return CommandHandleState.class;
        case ARRAY:
        case BOOLEAN_ARRAY:
        case INTEGER_ARRAY: 
        case REAL_ARRAY: 
        case STRING_ARRAY:
        	return PValueList.class;
        default:
        	throw new RuntimeException(this+" does not have a type class."); 
        }
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
    
    public boolean isSpecificType() {
    	return this != NUMERIC && this != UNKNOWN &&  this != ARRAY;
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
    
    /**
     * Takes a guess at what PlexilType this String is trying to indicate. 
     * @param originalName
     * @return
     */
    public static PlexilType fuzzyValueOf(String originalName) {
        
    	// Strip all symbols, make it all upper case
        String name = originalName.replaceAll("[^A-Za-z]", "").toUpperCase();
        
        if (name.startsWith("BOOL")) {
            if (name.contains("ARRAY")) {
                return BOOLEAN_ARRAY;
            } else {
                return BOOLEAN;
            }
        } else if (name.startsWith("INT")) {
            if (name.startsWith("INTERNAL")) {
                return UNKNOWN;
            }
            if (name.contains("ARRAY")) {
                return INTEGER_ARRAY;
            } else {
                return INTEGER;
            }
        } else if (name.startsWith("STR")) {
            if (name.contains("ARRAY")) {
                return STRING_ARRAY;
            } else {
                return STRING;
            }
        }
        
        // Just look for an easy match
        for (PlexilType type : values()) {
        	// Strip symbols here too
        	String thisOnesName = type.toString().replaceAll("_", "");
            if (thisOnesName.equalsIgnoreCase(name)) {
                return type;
            } 
        }
        
        throw new RuntimeException("Couldn't guess what "+originalName+" is.");
    }
    
    /**
     * Take a String and parse it into a PValue. If this is an array type, you
     * can pass in an array of Strings and each one will be parsed, and the 
     * resulting PlexilArray will be returned.
     * @param values
     * @return
     */
    public PValue parseValue(String value) {
        switch(this) {
        case UNKNOWN:
            return UnknownValue.get();
        case BOOLEAN:
            // (?i) means case insensitive.
            if (value.equals("1") || value.matches("(?i)T(rue)?")) {
                return BooleanValue.get(true);
            } else if (value.equals("0") || value.matches("(?i)F(alse)?")) {
                return BooleanValue.get(false);
            }
            return UnknownValue.get();
        case INTEGER:
            return IntegerValue.get(Integer.parseInt(value));
        case REAL:
        case NUMERIC:
            return RealValue.get(Double.parseDouble(value));
        case STRING:
            return StringValue.get(value);
        case STATE:
            return NodeState.valueOf(value);
        case OUTCOME:
            return NodeOutcome.valueOf(value);
        case FAILURE:
            return NodeFailureType.valueOf(value);
        case COMMAND_HANDLE:
            return CommandHandleState.valueOf(value);
        default:
        	throw new RuntimeException("Cannot parse values of "+this);
        }
    }
    
    /**
     * Get the class that represents this type. Could be an interface.
     * @param type
     * @return
     */
    public static Class<? extends PValue> getTypeClass(String type) {
        return valueOf(type).getTypeClass();
    }
    
    /**
     * Get the class that represents this type when it's not UNKNOWN.
     * @return
     */
    public Class<? extends PValue> getConcreteTypeClass() {
        switch(this) {
        case BOOLEAN: return BooleanValue.class;
        case INTEGER: return IntegerValue.class;
        case REAL: return RealValue.class;
        case STRING: return StringValue.class;
        default:
        return getTypeClass();
        }
    }
    
    /**
     * Get the class that represents this type when it's not UNKNOWN.
     * @return
     */
    public static Class<? extends PValue> getConcreteTypeClass(String type) {
        return valueOf(type).getConcreteTypeClass();
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
        return this == INTEGER || this == REAL || this == NUMERIC;
    }

}
