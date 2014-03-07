package edu.umn.crisys.plexil.il.statemachine;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.expr.ILEval;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PValue;

public class TransitionGuard {
    
    public static enum Description {
        START_CONDITION,
        SKIP_CONDITION,
        PRE_CONDITION,
        INVARIANT_CONDITION,
        REPEAT_CONDITION,
        POST_CONDITION,
        END_CONDITION,
        EXIT_CONDITION,

        ANCESTOR_ENDS_DISJOINED,
        ANCESTOR_EXITS_DISJOINED,
        ANCESTOR_INVARIANTS_CONJOINED,
        FAILURE_IS_PARENT_EXIT,
        FAILURE_IS_PARENT_FAIL,
        PARENT_EXECUTING,
        PARENT_WAITING,
        PARENT_FINISHED,
        
        COMMAND_ABORT_COMPLETE,
        COMMAND_ACCEPTED,
        COMMAND_DENIED,
        
        ALL_CHILDREN_FINISHED,
        ALL_CHILDREN_WAITING_OR_FINISHED,
        
        UPDATE_INVOCATION_SUCCESS;
        
        public String niceString() {
            String enumStr = toString();
            return Character.toUpperCase(enumStr.charAt(0))
                 + enumStr.substring(1).toLowerCase().replaceAll("_", " ");
        }
        
    }

    private Description description;
    private ILExpression expr;
    private Condition cond;
    
    public TransitionGuard(Description description, 
            ILExpression expression, Condition condition) {
        this.description = description;
        this.expr = expression;
        this.cond = condition;
    }
    
    public ILExpression getExpression() {
        return expr;
    }
    
    public Condition getCondition() {
    	return cond;
    }
    
    public boolean isAlwaysActive() {
        if ( ! ILEval.isConstant(expr) ) {
            return false; // There's no way to say this is always active.
        }
        // Check against the value it's supposed to return:
        return cond.checkValue(ILEval.eval(expr));
    }
    
    public boolean isNeverActive() {
        if ( ! ILEval.isConstant(expr) ) {
            return false; // There's no way to say this is never active.
        }
        // Does it always fail to return the right value?
        return ! cond.checkValue(ILEval.eval(expr));
   
    }
    
    @Override
    public String toString() {
        return "<"+description+" "+cond+"?> ("+expr.toString()+")";
    }
    
    public static enum Condition {
        TRUE,
        FALSE,
        UNKNOWN,
        NOTTRUE,
        NOTFALSE,
        KNOWN;
        
        public boolean checkValue(PValue v) {
            if ( ! (v instanceof PBoolean)) {
                throw new RuntimeException("Conditions should only be boolean, not "+v.getType());
            }
            PBoolean value = (PBoolean) v;
            switch (this) {
            case TRUE:
                return value.isTrue();
            case FALSE:
                return value.isFalse();
            case UNKNOWN:
                return value.isUnknown();
            case NOTTRUE:
                return value.isNotTrue();
            case NOTFALSE:
                return value.isNotFalse();
            case KNOWN:
                return value.isKnown();
            }
            throw new RuntimeException("Add this case to checkValue(): "+this);
        }
        
        @Override
        public String toString() {
            switch (this) {
            case TRUE:
                return "T";
            case FALSE:
                return "F";
            case UNKNOWN:
                return "U";
            case NOTTRUE:
                return "F, U";
            case NOTFALSE:
                return "T, U";
            case KNOWN:
                return "T, F";
            }
            return "some unknown condition";
        }
        
    }
    
}
