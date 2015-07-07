package edu.umn.crisys.plexil.test.java;

import java.util.List;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.PlanState;

public class PlanStateChecker extends TestOracle {

	private List<PlanState> expected;
	
	public PlanStateChecker(List<PlanState> expected) {
		this.expected = expected;
	}
	
	private void checkState(JavaPlan plan) {
        PlanState actual = plan.getSnapshot();
        List<String> failures = actual.testAgainst(expected.get(getMacroStep()-1));

        if (failures.size() > 0) {
            String errorMsg = "Failures: ";
            for (String failure : failures) {
                errorMsg += "\n"+failure;
            }
            throw new RuntimeException(errorMsg);
        }

	}
	
	@Override
	public void beforePlanExecution(JavaPlan plan) {
		//checkState(plan);
	}

	@Override
	public void endOfMacroStep(JavaPlan plan) {
		checkState(plan);
	}

	@Override
	public void endOfExecution(JavaPlan plan) {
		// We should be out of stuff to check, if not something might be wrong
		if (expected.size() != getMacroStep()) {
			System.err.println("Warning: Execution ended at step "+getMacroStep()
					+", but I have "+expected.size()+" snapshots.");
		}
	}

}
