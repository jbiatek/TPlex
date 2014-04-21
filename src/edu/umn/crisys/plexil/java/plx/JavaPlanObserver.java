package edu.umn.crisys.plexil.java.plx;

public interface JavaPlanObserver {

	
	/**
	 * Called after PLEXIL has reached quiescence. When something changes,
	 * return so that PLEXIL can run again, this time with the new event. 
	 */
	public void quiescenceReached(JavaPlan plan);
	
	/**
	 * Called if the PLEXIL plan ended its macro step early due to an
	 * assignment, command, or update. 
	 */
	public void prematureEndOfMacroStep(JavaPlan plan);
	
	/**
	 * Called at the end of every micro step. External worlds should NOT
	 * change state, as the PLEXIL semantics dictate that the world does not 
	 * change during a macro step. This is intended for information collection
	 * only. 
	 * @param plan
	 */
	public void endOfMicroStep(JavaPlan plan);
}
