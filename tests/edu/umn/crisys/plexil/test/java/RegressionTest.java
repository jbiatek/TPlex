package edu.umn.crisys.plexil.test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;


/**
 * How to run regression tests:
 *      GenerateOracleScript.java creates generate_logs.sh based on the 
 *          information here about tests (test suites, same name tests, empty
 *          script tests, etc)
 *      generate_logs.sh creates log files by running plexiltest and puts them
 *          in the resources package.
 *      CompileRegressionTest also uses the test information here, this time to
 *          translate the files from resources to Java, and puts them in the
 *          tests directory.
 *      Finally, these tests compare the behavior of the Java code with the 
 *          log files, making sure they don't contradict one another.
 * 
 * 
 * @author jbiatek
 *
 */
public class RegressionTest {
	
	public static final File TESTING_DIRECTORY = new File("tests");
	public static final File RESOURCES = new File("tests/edu/umn/crisys/plexil/test/resources/");
	public static final File ORACLE_LOGS = new File("tests/generated/oracle");
	public static final File LUSTRE_FILES = new File("tests/generated/lustre");
	public static final String TPLEX_OUTPUT_PACKAGE = "generated.java";
	
	public static class TestSuite {
		public String planFile;
		public String[] planScripts;
		public String[] libs;
		
		public TestSuite(String planName, String[] planScripts, String[] libs) {
			this.planFile = planName;
			this.planScripts = planScripts;
			this.libs = libs;
		}
	}	
	
	private static TestSuite simple_drive_r = new TestSuite("simple-drive", 
            new String[] {"single-drive", "double-drive"}, new String[]{});
	
	private static TestSuite increment1 = new TestSuite("Increment-test",
			new String[] {}, new String[]{"Increment"});
	
	private static TestSuite increment2 = new TestSuite("Increment-test2",
			new String[] {}, new String[]{"Increment"});
	
	/**
	 * Complex tests that were specified by hand
	 */
	private static final TestSuite[] MANUAL_TESTS = new TestSuite[] {
		simple_drive_r, increment1, increment2
	};
	
	/**
	 * Tests where the script name matches the plan name
	 */
	private static final String[] SAME_NAME_TESTS = new String[] {
	    "CruiseControl", "DriveToSchool", "DriveToTarget", "SafeDrive", "SimpleDrive",
	    "array1", "array3", "array4", "array8", "command1", "command2", "command3",
	    "command4", "command5", "long_command", "uncle_command", "repeat2", 
	    "repeat5", "repeat7", "repeat8", "atomic-assignment", "boolean1", 
	    "change-lookup-test"
	};

	/**
	 * Tests that don't have a script at all
	 */
	private static final String[] EMPTY_SCRIPT_TESTS = new String[] {
	    "AncestorReferenceTest", "assign-failure-with-conflict", 
	    "assign-to-parent-exit", "assign-to-parent-invariant", 
	    "array2", "array5", "array6", "array9", "failure-type1", "failure-type2",
	    "failure-type3", "failure-type4", "array-in-loop", "Increment-test",
	    "Increment-test2", "repeat1", "repeat3", "repeat4"
	};
	
	private static TestSuite produceSameNameTest(String name) {
		return new TestSuite(name, new String[] { name }, new String[]{});
	}
	
	private static TestSuite produceEmptyScriptTest(String name) {
		return new TestSuite(name, new String[] { }, new String[]{});
	}
	
	/**
	 * @return all of the TestSuites in the regression test suite for Java.
	 */
	public static List<TestSuite> getJavaTestSuites() {
		List<TestSuite> ret = new ArrayList<RegressionTest.TestSuite>();
		ret.addAll(Arrays.asList(MANUAL_TESTS));
		
		for (String name : RegressionTest.SAME_NAME_TESTS) {
			ret.add(produceSameNameTest(name));
		}

		for (String name : EMPTY_SCRIPT_TESTS) {
			ret.add(produceEmptyScriptTest(name));
		}
		
		return ret;
	}
	
	/**
	 * @return all of the TestSuites for Lustre regression testing. 
	 */
	public static List<TestSuite> getLustreTestSuites() {
		// Preeeeety limited for now. 
		return Arrays.asList(produceSameNameTest("DriveToSchool"));
	}
	
	
	@Before
	public void debug() {
		JavaPlan.DEBUG = true;
		PlanState.DEBUG = true;
	}
	
	@Test
	public void CruiseControl() throws Exception {
		runSingleTest("CruiseControl", "CruiseControl");
	}
	
	@Test
	public void DriveToSchool() throws Exception {
		runSingleTest("DriveToSchool", "DriveToSchool");
	}
	
	@Test
	public void DriveToTarget() throws Exception {
		runSingleTest("DriveToTarget", "DriveToTarget");
	}
	
	@Test
	public void SafeDrive() throws Exception {
		runSingleTest("SafeDrive", "SafeDrive");
	}
	
	@Test
	public void SimpleDrive() throws Exception {
		runSingleTest("SimpleDrive", "SimpleDrive");
	}
	
	@Test
	public void simple_drive() throws Exception {
	    runTestSuite(simple_drive_r);
	}

	@Test
	public void AncestorReferenceTest() throws Exception {
	    runSingleTest("AncestorReferenceTest", "empty");
	}
	
	@Test @Ignore
    public void assign_failure_with_conflict() throws Exception {
        runSingleTest("assign-failure-with-conflict", "empty");
    }
	
	@Test
    public void assign_to_parent() throws Exception {
        runSingleTest("assign-to-parent-exit", "empty");
        runSingleTest("assign-to-parent-invariant", "empty");
    }
	
	@Test
    public void atomic_assignment() throws Exception {
        runSingleTest("atomic-assignment", "atomic-assignment");
    }
	
	public void boolean1() throws Exception {
	    runSingleTest("boolean1", "boolean1");
	}

	public void change_lookup_test() throws Exception {
        runSingleTest("change-lookup-test", "change-lookup-test");
    }
	
    @Test
	public void long_command() throws Exception {
	    runSingleTest("long_command", "long_command");
	}
	
	@Test
	public void uncle_command() throws Exception {
	    runSingleTest("uncle_command", "uncle_command");
	}
	
	@Test
	public void arrayTests() throws Exception {
	    runSingleTest("array-in-loop", "empty");
	    runSingleTest("array1", "array1");
	    runSingleTest("array2", "empty");
	    runSingleTest("array3", "array3");
	    runSingleTest("array4", "array4");
	    runSingleTest("array5", "empty");
	    runSingleTest("array6", "empty");
	    runSingleTest("array8", "array8");
	    runSingleTest("array9", "empty");
	}
	
	   
    @Test
    public void repeatTests() throws Exception {
        runSingleTest("repeat1", "empty");
        runSingleTest("repeat2", "repeat2");
        runSingleTest("repeat3", "empty");
        runSingleTest("repeat4", "empty");
        runSingleTest("repeat5", "repeat5");
        runSingleTest("repeat7", "repeat7");
        runSingleTest("repeat8", "repeat8");
    }
	
	@Test
	public void commandTests() throws Exception {
	    runSingleTest("command1", "command1");
	    runSingleTest("command2", "command2");
	    runSingleTest("command3", "command3");
	    runSingleTest("command4", "command4");
	    runSingleTest("command5", "command5");
	}
	
	@Test
	public void failureTypeTests() throws Exception {
	    runSingleTest("failure-type1", "empty");
        runSingleTest("failure-type2", "empty");
        runSingleTest("failure-type3", "empty");
        runSingleTest("failure-type4", "empty");

	}
	
	@Test
	public void incrementTests() throws Exception {
	    runTestSuite(increment1);
	    runTestSuite(increment2);
	}
	
	private List<PlanState> parseLogFile(File logFile) throws Exception {
		List<PlanState> oracles = new ArrayList<PlanState>();
		BufferedReader in = new BufferedReader(new FileReader(logFile));
		PlanState state = PlanState.parseLogFile(in);
		while (state != null) {
			if (PlanState.DEBUG) 
				System.out.println("*******************************");
			oracles.add(state);
			state = PlanState.parseLogFile(in);
		}
		return oracles;
	}

	private void runSingleTest(String planName, String scriptName) throws Exception {
		List<PlanState> expected = parseLogFile(
		        new File(ORACLE_LOGS, planName+"___"+scriptName+".log"));
		
		assertTrue("Check the log file, nothing was pulled from it.", expected.size() > 0);

		runSingleTestJava(planName, scriptName, expected);
	}
	
	private void runSingleTestJava(String planName, String scriptName, 
			List<PlanState> expected) throws Exception {
        System.out.println("Running "+planName+" with script "+scriptName);
        
	    String script = NameUtils.clean(scriptName);
//		root.fullReset();
		// Get the script
		JavaPlexilScript world;
		if (scriptName.equals("empty")) {
		    world = new JavaPlexilScript();
		} else {
		    Class<?> scriptClass = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+script+"Script");
		    world = (JavaPlexilScript) 
		        scriptClass.getConstructor().newInstance();
		}
		// Need to find the root node
		Class<?> main = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+NameUtils.clean(planName));
		PlexilTestable root = (PlexilTestable) main.getConstructor(ExternalWorld.class).newInstance(world);
		
		// And finally the log file
        
        System.out.println("World set, ready to go");
        for (int i = 0; i < expected.size(); i++) {
            System.out.println("==> Starting step "+(i+1));
            root.doMacroStep();
            
            // Do the test for equality:
            PlanState actual = root.getSnapshot();
            List<String> failures = actual.testAgainst(expected.get(i));

            if (failures.size() > 0) {
                String errorMsg = "Failures: ";
                for (String failure : failures) {
                    errorMsg += "\n"+failure;
                }
                throw new RuntimeException(errorMsg);
            }
            System.out.println("==> Ending step "+(i+1));
            
            world.quiescenceReached((JavaPlan)root);
        }
        System.out.println("Finished.");
	}
	
	private void runTestSuite(TestSuite suite) throws Exception {
	    for (String script : suite.planScripts) {
	        runSingleTest(suite.planFile, script);
	    }
	}
	
}
