package edu.umn.crisys.plexil.test.java;


import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import generated.java.DriveToSchoolScript;

import org.junit.Test;

public class SimulatorTest {

	
	@Test
	public void driveToSchool() throws Exception {
		ExternalWorld world = new DriveToSchoolScript();
		ILSimulator sim = new ILSimulator(RegressionTest.getPlanAsIL("DriveToSchool"), world);
		
		RegressionTest.runTest(sim, world, 
				RegressionTest.parseLogFile("DriveToSchool", "DriveToSchool"));
	}
}
