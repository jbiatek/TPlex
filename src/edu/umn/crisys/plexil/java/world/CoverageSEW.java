package edu.umn.crisys.plexil.java.world;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.java.plx.StateCoverageMeasurer;
import edu.umn.crisys.plexil.java.psx.symbolic.SPFValues;
import gov.nasa.jpf.vm.Verify;

public class CoverageSEW extends SymbolicExternalWorld {

	private StateCoverageMeasurer coverage = new StateCoverageMeasurer();
	private int previousCoverage = 0;
	private int consecutiveStepsWithoutIncrease = 0;
	
	public CoverageSEW() {
		super(new SPFValues());
	}
	
	public JavaPlanObserver getObserver() {
		return coverage;
	}
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		super.quiescenceReached(plan);
		//noteCoverage();
	}

	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		super.prematureEndOfMacroStep(plan);
		//noteCoverage();
	}
	
	private void noteCoverage() {
		int newCoverage = coverage.getNumStatesCovered();
		if (newCoverage > previousCoverage) {
			Verify.interesting(true);
			consecutiveStepsWithoutIncrease = 0;
		} else {
			consecutiveStepsWithoutIncrease++;
			
			if (consecutiveStepsWithoutIncrease > 5) {
				Verify.ignoreIf(true);
			}
		}
		
		previousCoverage = newCoverage;
	}
	
	
}
