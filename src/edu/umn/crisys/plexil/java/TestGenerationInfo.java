package edu.umn.crisys.plexil.java;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.psx.symbolic.SymbolicDecisionMaker;
import edu.umn.crisys.plexil.java.psx.symbolic.ValueSource;
import edu.umn.crisys.plexil.java.world.ExternalWorld;

public interface TestGenerationInfo {

	public JavaPlan createPlanUnderTest(ExternalWorld world);

	public SymbolicDecisionMaker createDecisionMaker(ValueSource values);

	public ValueSource createSymbolicValueSource();
}
