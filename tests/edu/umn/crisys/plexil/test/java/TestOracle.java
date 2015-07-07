package edu.umn.crisys.plexil.test.java;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;

/**
 * The TestOracle observer adds some step counting because usually we're 
 * reading from a file or something, and need to keep track of where we are.
 * @author jbiatek
 *
 */
public abstract class TestOracle implements JavaPlanObserver {

	private int microStepCounter = 0;
	private int macroStepCounter = 0;
	
	public int getMicroStep() {
		return microStepCounter;
	}
	
	public int getMacroStep() {
		return macroStepCounter;
	}

	@Override
	public final void quiescenceReached(JavaPlan plan) {
		// Increment the counter, then the subclass can do whatever
		macroStepCounter++;
		checkQuiescenceReached(plan);
	}

	public void checkQuiescenceReached(JavaPlan plan) {
		// If they don't care how the step ended, they'll use this
		endOfMacroStep(plan);
	}
	
	@Override
	public final void prematureEndOfMacroStep(JavaPlan plan) {
		// Increment the counter, then the subclass can do whatever
		macroStepCounter++;
		checkPrematureEndOfMacroStep(plan);
	}

	public void checkPrematureEndOfMacroStep(JavaPlan plan) {
		// If they don't care how the step ended, they'll use this
		endOfMacroStep(plan);
	}

	@Override
	public void endOfMicroStepAfterCommit(JavaPlan plan) {
		// Increment the counter, then the subclass can do whatever
		microStepCounter++;
		checkEndOfMicroStepAfterCommit(plan);
	}

	public void checkEndOfMicroStepAfterCommit(JavaPlan plan) {
		// Default method does nothing
	}

}
