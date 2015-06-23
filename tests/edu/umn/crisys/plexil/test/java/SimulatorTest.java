package edu.umn.crisys.plexil.test.java;

import java.io.File;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.main.Main;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import generated.java.DriveToSchoolScript;

import org.junit.Test;

public class SimulatorTest {

	public static Plan getPlanAsIL(String name) {
		Main tplex = new Main();
		
		tplex.files.add(new File(RegressionTest.RESOURCES, name+".plx"));
		tplex.outputLanguage = Main.OutputLanguage.NONE;
		
		tplex.execute();
		
		if (tplex.ilPlans.size() != 1) {
			throw new RuntimeException("Which one do I want? ");
		}
		
		for (Plan plan : tplex.ilPlans) {
			return plan;
		}
		throw new RuntimeException("this is unreachable");
	}
	
	@Test
	public void driveToSchool() throws Exception {
		ExternalWorld world = new DriveToSchoolScript();
		ILSimulator sim = new ILSimulator(getPlanAsIL("DriveToSchool"), world);
		
		RegressionTest.runTest(sim, world, 
				RegressionTest.parseLogFile("DriveToSchool", "DriveToSchool"));
	}
}
