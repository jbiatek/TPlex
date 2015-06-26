package edu.umn.crisys.plexil.runtime.plx;

public interface JavaPlanObserver {

	
	/**
	 * Called after PLEXIL has reached quiescence. When something changes,
	 * return so that PLEXIL can run again, this time with the new event. 
	 */
	public default void quiescenceReached(JavaPlan plan) {
		
	}
	
	/**
	 * Called if the PLEXIL plan ended its macro step early due to an
	 * assignment, command, or update. Any environment variable is allowed
	 * to change at this time, and presumably an event has just been issued
	 * to the environment that could also be responded to. 
	 */
	public default void prematureEndOfMacroStep(JavaPlan plan) {
		
	}
	
	/**
	 * Called at the end of every micro step, before variables have been 
	 * committed.  
	 * 
	 * External worlds should NOT
	 * change state, as the PLEXIL semantics dictate that the world does not 
	 * change during a macro step. This is intended for information collection
	 * only. 
	 * @param plan
	 */
	public default void endOfMicroStepBeforeCommit(JavaPlan plan) {
		
	}
	
	/**
	 * Called at the end of every micro step after variables have been 
	 * committed.
	 * 
	 * External worlds should NOT change state, since the environment is not 
	 * allowed to change except between macro steps. 
	 * @param plan
	 */
	public default void endOfMicroStepAfterCommit(JavaPlan plan) {
		
	}
	
	/**
	 * Called when execution has halted.
	 */
	public default void endOfExecution(JavaPlan plan) {
		
	}
}
