package edu.umn.crisys.plexil.il;

public enum PlexilExprDescription {
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