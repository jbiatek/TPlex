package edu.umn.crisys.plexil.test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jkind.lustre.values.Value;
import lustre.LustreProgram;
import lustre.LustreTrace;
import lustre.LustreVariable;
import main.LustreMain;

import org.junit.*;

import simulation.LustreSimulator;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.ILExprToLustre;
import edu.umn.crisys.plexil.main.TPlex;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
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
	public void turnOnVerbosity() {
		debugOn();
	}
	
	public static void debugOn() {
		JavaPlan.DEBUG = true;
		PlanState.DEBUG = true;
	}
	
	public static void debugOff() {
		JavaPlan.DEBUG = false;
		PlanState.DEBUG = false;
	}
	
	@Test
	public void CruiseControl() throws Exception {
		runSingleTestJava("CruiseControl", "CruiseControl");
	}
	
	@Test
	public void DriveToSchool() throws Exception {
		runSingleTestJava("DriveToSchool", "DriveToSchool");
		runSingleTestLustre("DriveToSchool", "DriveToSchool");
	}
	
	@Test
	public void DriveToTarget() throws Exception {
		runSingleTestJava("DriveToTarget", "DriveToTarget");
	}
	
	@Test
	public void SafeDrive() throws Exception {
		runSingleTestJava("SafeDrive", "SafeDrive");
	}
	
	@Test
	public void SimpleDrive() throws Exception {
		runSingleTestJava("SimpleDrive", "SimpleDrive");
	}
	
	@Test
	public void simple_drive() throws Exception {
	    runTestSuite(simple_drive_r);
	}

	@Test
	public void AncestorReferenceTest() throws Exception {
	    runSingleTestJava("AncestorReferenceTest", "empty");
	}
	
	@Test @Ignore
    public void assign_failure_with_conflict() throws Exception {
        runSingleTestJava("assign-failure-with-conflict", "empty");
    }
	
	@Test
    public void assign_to_parent() throws Exception {
        runSingleTestJava("assign-to-parent-exit", "empty");
        runSingleTestJava("assign-to-parent-invariant", "empty");
    }
	
	@Test
    public void atomic_assignment() throws Exception {
        runSingleTestJava("atomic-assignment", "atomic-assignment");
    }
	
	public void boolean1() throws Exception {
	    runSingleTestJava("boolean1", "boolean1");
	}

	public void change_lookup_test() throws Exception {
        runSingleTestJava("change-lookup-test", "change-lookup-test");
    }
	
    @Test
	public void long_command() throws Exception {
	    runSingleTestJava("long_command", "long_command");
	}
	
	@Test
	public void uncle_command() throws Exception {
	    runSingleTestJava("uncle_command", "uncle_command");
	}
	
	@Test
	public void arrayTests() throws Exception {
	    runSingleTestJava("array-in-loop", "empty");
	    runSingleTestJava("array1", "array1");
	    runSingleTestJava("array2", "empty");
	    runSingleTestJava("array3", "array3");
	    runSingleTestJava("array4", "array4");
	    runSingleTestJava("array5", "empty");
	    runSingleTestJava("array6", "empty");
	    runSingleTestJava("array8", "array8");
	    runSingleTestJava("array9", "empty");
	}
	
	   
    @Test
    public void repeatTests() throws Exception {
        runSingleTestJava("repeat1", "empty");
        runSingleTestJava("repeat2", "repeat2");
        runSingleTestJava("repeat3", "empty");
        runSingleTestJava("repeat4", "empty");
        runSingleTestJava("repeat5", "repeat5");
        runSingleTestJava("repeat7", "repeat7");
        runSingleTestJava("repeat8", "repeat8");
    }
	
	@Test
	public void commandTests() throws Exception {
	    runSingleTestJava("command1", "command1");
	    runSingleTestJava("command2", "command2");
	    runSingleTestJava("command3", "command3");
	    runSingleTestJava("command4", "command4");
	    runSingleTestJava("command5", "command5");
	}
	
	@Test
	public void failureTypeTests() throws Exception {
	    runSingleTestJava("failure-type1", "empty");
        runSingleTestJava("failure-type2", "empty");
        runSingleTestJava("failure-type3", "empty");
        runSingleTestJava("failure-type4", "empty");

	}
	
	@Test
	public void incrementTests() throws Exception {
	    runTestSuite(increment1);
	    runTestSuite(increment2);
	}
	
	public static List<PlanState> parseLogFile(String planName, String scriptName) throws Exception{
		return parseLogFile(
		        new File(ORACLE_LOGS, planName+"___"+scriptName+".log"));
	}
	
	public static List<PlanState> parseLogFile(File logFile) throws Exception {
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
	
	public static Plan getPlanAsIL(String name) {
		TPlex tplex = new TPlex();
		
		tplex.files.add(new File(RegressionTest.RESOURCES, name+".plx"));
		tplex.outputLanguage = TPlex.OutputLanguage.NONE;
		
		tplex.execute();
		
		if (tplex.ilPlans.size() != 1) {
			throw new RuntimeException("Which one do I want? ");
		}
		
		for ( Entry<String, Plan> entry : tplex.ilPlans.entrySet()) {
			return entry.getValue();
		}
		throw new RuntimeException("this is unreachable");
	}

	public static JavaPlexilScript getScript(String scriptName) throws ReflectiveOperationException {
		if (scriptName.equals("empty")) {
		    return new JavaPlexilScript();
		} else {
		    Class<?> scriptClass = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+scriptName+"Script");
		    return (JavaPlexilScript) 
		        scriptClass.getConstructor().newInstance();
		}
	}
	
	public static void runSingleTestJava(String planName, String scriptName) throws Exception {
		List<PlanState> expected = parseLogFile(planName, scriptName);
		
		assertTrue("Check the log file, nothing was pulled from it.", expected.size() > 0);

		
        System.out.println("Running "+planName+" with script "+scriptName);
        
//		root.fullReset();
		// Get the script
		JavaPlexilScript world = getScript(scriptName);
		// Need to find the root node
		Class<?> main = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+NameUtils.clean(planName));
		PlexilTestable root = (PlexilTestable) main.getConstructor(ExternalWorld.class).newInstance(world);
		
		runTest(root, world, expected);
	}
	
	public static void runSingleTestLustre(String planName, String scriptName) throws Exception {
		// We're actually going to use the IL simulator to test the Lustre code. 
		// So first, make sure that the simulator matches the executive logs.
		JavaPlexilScript script = getScript(scriptName);
		Plan ilPlan = getPlanAsIL(planName);
		List<PlanState> expected = parseLogFile(planName, scriptName);
		ILSimulator sim = new ILSimulator(ilPlan, script);
		debugOff();
		runTest(sim, script, expected);
		debugOn();

		// Okey dokey, if we got to here, our IL plan conforms to the oracle.
		// Now we compare the IL plan to the Lustre plan. 
		// Reset everything so we can start again
		script.reset();
		sim.reset();
		
		// Simulate the Lustre code and get trace data out
		Map<String, LustreVariable> stringTrace = new HashMap<>();
		LustreTrace traceWrap = getLustreTraceData(planName, scriptName);
		traceWrap.getVariableNames().forEach(
				name -> stringTrace.put(name, traceWrap.getVariable(name)));
		
		// Swap out these Strings for IL expressions where possible.
		final Map<ILExpression, LustreVariable> ilTrace = new HashMap<>();
		// States aren't stored as variables, so those first
		ilPlan.getMachines().forEach(nsm -> 
				nsm.getNodeIds().forEach(uid -> 
				attachILExprToLustreVar(new GetNodeStateExpr(uid), 
						stringTrace, ilTrace)));
		// Then all variables from the plan
		ilPlan.getVariables().forEach(var -> 
				attachILExprToLustreVar(var, stringTrace, ilTrace));
		
		// Attach an observer to check these values against the IL sim
		sim.addObserver(new JavaPlanObserver() {

			private int step = 0;
			private List<String> errors = new ArrayList<String>();
			
			@Override
			public void endOfMicroStep(JavaPlan plan) {
				ILSimulator sim = (ILSimulator) plan;
				for (ILExpression expr : ilTrace.keySet()) {
					checkValue(expr, sim);
				}
				if (errors.size() != 0) {
					throw new RuntimeException(errors.stream().collect(Collectors.joining(", ")));
				} else {
					step++;
				}
			}
			
			private void checkValue(ILExpression e, ILSimulator sim) {
				PValue expected = sim.eval(e);
				Value actual = ilTrace.get(e).getValue(step);
				String expectedStr = hackyILExprToLustre(expected, e.getType());
				
				//TODO: is this really the best way to compare them?
				if ( ! expectedStr.equals(actual.toString())) {
					errors.add("Values don't match: expected "+expected+", "
							+"which should be "+expectedStr
							+" in Lustre, but instead saw "+actual);
				}
			}
			
		});
		
		// Here we go!
		sim.runPlanToCompletion();
	}
	
	private static String hackyILExprToLustre(ILExpression e, PlexilType type) {
		ILExprToLustre il2lustre = new ILExprToLustre();
		String lustreString = ILExprToLustre.exprToString(e.accept(il2lustre, type));
		//TODO: This is a massive hack, there should be a better way to do this
		return lustreString.replaceFirst("^\\(pre ", "").replaceFirst("\\)$", "");
	}
	
	private static void attachILExprToLustreVar(ILExpression e, 
			Map<String, LustreVariable> stringTrace,
			Map<ILExpression, LustreVariable> ilTrace) {
		String lustreString = hackyILExprToLustre(e, e.getType());
		
		if (stringTrace.containsKey(lustreString)) {
			ilTrace.put(e, stringTrace.get(lustreString));
		} else {
			System.err.println("Warning: Didn't find IL expression in Lustre trace: "
					+e+", in Lustre as "+lustreString);
		}
	}
	
	
	public static LustreTrace getLustreTraceData(String planName, String scriptName) throws Exception{
		LustreMain.initialize();
		
		File lustreFile = new File(LUSTRE_FILES, planName+".lus");
		File inputCsv = new File(LUSTRE_FILES, planName+"__"+scriptName+".csv");

		LustreProgram program = new LustreProgram(lustreFile.getPath(), planName);
		
		List<LustreTrace> inputs = testsuite.TestSuite
				.readTestsFromFile(program, inputCsv.getPath());
		LustreSimulator lustreSim = new LustreSimulator(program);
		
		
		
		List<LustreTrace> ret = lustreSim.simulate(inputs, null);
		
		LustreMain.terminate();
		
		assertEquals("Only should have gotten 1 trace back", 1, ret.size());
		
		return ret.get(0);
	}
	
	
	public static void runTest(PlexilTestable root, ExternalWorld world, 
			List<PlanState> expected) {
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
	
	public static void runTestSuite(TestSuite suite) throws Exception {
	    for (String script : suite.planScripts) {
	        runSingleTestJava(suite.planFile, script);
	    }
	}
	
}
