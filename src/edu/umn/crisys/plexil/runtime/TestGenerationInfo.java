package edu.umn.crisys.plexil.runtime;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.symbolic.SymbolicDecisionMaker;
import edu.umn.crisys.plexil.runtime.psx.symbolic.ValueSource;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;

public interface TestGenerationInfo {

	public JavaPlan createPlanUnderTest(ExternalWorld world);

	public SymbolicDecisionMaker createDecisionMaker(ValueSource values);

	public ValueSource createSymbolicValueSource();
}
