package edu.umn.crisys.plexil.translator.il;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;

enum TransitionSideEffect {
	NONE,
	OUTCOME_SKIPPED,
	OUTCOME_SUCCESS,
	OUTCOME_FAILURE_PRECONDITION,
	OUTCOME_FAILURE_POSTCONDITION_FAILED,
	OUTCOME_FAILURE_PARENT_FAILED, 
	OUTCOME_FAILURE_INVARIANT_FAILED,
	OUTCOME_INTERRUPTED_PARENT_EXITED,
    OUTCOME_INTERRUPTED_EXITED,
	ABORT,
	PERFORM_RESTORE_ASSIGNMENT,// New behavior!!!!
	RESET;
	
	public void addSideEffect(JBlock block, JCodeModel cm) {
		switch (this) {
		case NONE:
			break;
		case OUTCOME_SKIPPED:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.SKIPPED.toString())));
			break;
		case OUTCOME_SUCCESS:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.SUCCESS.toString())));
			break;
		case OUTCOME_FAILURE_PRECONDITION:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.FAILURE.toString())));
			block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
					.staticRef(NodeFailureType.PRE_CONDITION_FAILED.toString())));
			break;
		case OUTCOME_FAILURE_POSTCONDITION_FAILED:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.FAILURE.toString())));
			block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
					.staticRef(NodeFailureType.POST_CONDITION_FAILED.toString())));
			break;
		case OUTCOME_FAILURE_PARENT_FAILED:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.FAILURE.toString())));
			block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
					.staticRef(NodeFailureType.PARENT_FAILED.toString())));
			break;
		case OUTCOME_FAILURE_INVARIANT_FAILED:
			block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
					.staticRef(NodeOutcome.FAILURE.toString())));
			block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
					.staticRef(NodeFailureType.INVARIANT_CONDITION_FAILED.toString())));
			break;
			
			
		case OUTCOME_INTERRUPTED_PARENT_EXITED:
		    block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
		            .staticRef(NodeOutcome.INTERRUPTED.toString())));
		    block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
		            .staticRef(NodeFailureType.PARENT_EXITED.toString())));
		    break;
		    
		case OUTCOME_INTERRUPTED_EXITED:
		    block.add(JExpr.invoke("setOutcome").arg(cm.ref(NodeOutcome.class)
                    .staticRef(NodeOutcome.INTERRUPTED.toString())));
            block.add(JExpr.invoke("setFailure").arg(cm.ref(NodeFailureType.class)
                    .staticRef(NodeFailureType.EXITED.toString())));		    
		    break;
		case ABORT:
			block.add(JExpr.invoke("invokeAbort"));
			break;
		case PERFORM_RESTORE_ASSIGNMENT:
			block.add(JExpr.invoke("assignUnknown"));
			break;
		case RESET:
			block.add(JExpr.invoke("reset"));
			break;
		}
	}
	
	@Override
	public String toString() {
	       switch (this) {
	        case NONE:
	            return "No side effect";
	        case OUTCOME_SKIPPED:
	            return "Mark outcome as SKIPPED";
	        case OUTCOME_SUCCESS:
	            return "Mark outcome as SUCCESS";
	        case OUTCOME_FAILURE_PRECONDITION:
	            return "Mark outcome as FAILURE, type PRE_CONDITION_FAILED";
	        case OUTCOME_FAILURE_POSTCONDITION_FAILED:
	            return "Mark outcome as FAILURE, type POST_CONDITION_FAILED";
	        case OUTCOME_FAILURE_PARENT_FAILED:
	            return "Mark outcome as FAILURE, type PARENT_FAILED";
	        case OUTCOME_FAILURE_INVARIANT_FAILED:
	            return "Mark outcome as FAILURE, type INVARIANT_CONDITION_FAILED";
	        case OUTCOME_INTERRUPTED_PARENT_EXITED:
	            return "Mark outcome as INTERRUPTED, type PARENT_EXITED";
	            
	        case OUTCOME_INTERRUPTED_EXITED:
	            return "Mark outcome as INTERRUPTED, type EXITED";
	        case ABORT:
	            return "Abort";
	        case PERFORM_RESTORE_ASSIGNMENT:
	            return "Restore previous value";
	        case RESET:
	            return "Reset";
	        }
	       return "Something that should be added to toString()";
	}
}