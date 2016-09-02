package edu.umn.crisys.plexil.il.expr;

import java.util.Optional;

import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PNumeric;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;
import edu.umn.crisys.plexil.script.translator.ScriptParser;

public enum ILType {

	NATIVE_BOOL     (Optional.empty()),
    BOOLEAN			(Optional.of(UnknownValue.get())),
    INTEGER			(Optional.of(UnknownValue.get())),
    REAL			(Optional.of(UnknownValue.get())),
    STRING			(Optional.of(UnknownValue.get())),
    //NUMERIC			(Optional.of(UnknownValue.get())),
    UNKNOWN			(Optional.of(UnknownValue.get())),
    BOOLEAN_ARRAY	(Optional.empty()),
    INTEGER_ARRAY   (Optional.empty()),
    REAL_ARRAY      (Optional.empty()),
    STRING_ARRAY    (Optional.empty()),
    STATE			(Optional.empty()),
    OUTCOME			(Optional.of(NodeOutcome.UNKNOWN)),
    FAILURE			(Optional.of(NodeFailureType.UNKNOWN)),
    COMMAND_HANDLE	(Optional.of(CommandHandleState.UNKNOWN));
    // TODO: TIME ??????

    private final Optional<PValue> unknown;

    private ILType(Optional<PValue>unknown) {
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
        //case NUMERIC: return PNumeric.class;
        case STATE: return NodeState.class;
        case OUTCOME: return NodeOutcome.class;
        case FAILURE: return NodeFailureType.class;
        case COMMAND_HANDLE: return CommandHandleState.class;
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
    public void typeCheck(ILType other) {
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
        // They better just be the same at this point.
        if (this != other) {
            throw new RuntimeException("Type error: Trying to use "+other+" as a "+this);
        }        
    }
    
    /**
     * Ensure that the given type is exactly compatible with this one. Use this
     * to ensure that all types involved are IL compatible: they must both be
     * 
     * 
     * @param other
     */
    public void strictTypeCheck(ILType other) {
    	if (this == ILType.UNKNOWN) {
    		throw new RuntimeException("Type failure: The left hand type was UNKNOWN");
    	}
    	if (other == ILType.UNKNOWN) {
    		throw new RuntimeException("Type failure: The right hand type was UNKNOWN");
    	}
    	if (this != other) {
    		throw new RuntimeException("Type failure: "+this+" does not match "+other);
    	}
    }
    
    public void strictTypeCheck(ILExpr expression) {
    	if (expression instanceof UnknownValue) {
    		// This is going to say UNKNOWN. As long as this is one of the
    		// main IL types, this is okay.
    		if (this == ILType.BOOLEAN
    				|| this == INTEGER
    				|| this == REAL
    				|| this == STRING) {
    			return;
    		}
    	}
    	strictTypeCheck(expression.getType());
    }
    
    public boolean isSpecificType() {
    	return this != UNKNOWN;
    }
    
    public ILType getMoreSpecific(ILType other) {
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
    public ILType toArrayType() {
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
    public ILType elementType() {
        switch (this) {
        case BOOLEAN_ARRAY:
            return BOOLEAN;
        case INTEGER_ARRAY:
            return INTEGER;
        case REAL_ARRAY:
            return REAL;
        case STRING_ARRAY:
            return STRING;
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
        return this == INTEGER || this == REAL;// || this == NUMERIC;
    }

}
