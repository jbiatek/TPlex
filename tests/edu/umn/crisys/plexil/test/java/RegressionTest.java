package edu.umn.crisys.plexil.test.java;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lustre.LustreTrace;

import org.junit.*;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.il2lustre.ScriptSimulation;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.plexil.main.TPlex;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.PlanState;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.util.NameUtils;


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
	public static final File RESOURCES = new File("tests/edu/umn/crisys/plexil/test/plxfiles/");
	public static final File ORACLE_LOGS = new File("tests/generated/oracle");
	public static final File LUSTRE_FILES = new File("tests/generated/lustre");
	public static final String TPLEX_OUTPUT_PACKAGE = "generated.java";
	
	public static boolean SIMULATE_ALL_JAVA_FILES = false;
	
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
	
	private static TestSuite simple_drive_r = new TestSuite("SimpleDriveRegressionTest", 
            new String[] {"single-drive", "double-drive"}, new String[]{});
	
	/**
	 * Complex tests that were specified by hand
	 */
	private static final TestSuite[] MANUAL_TESTS = new TestSuite[] {
		simple_drive_r
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
	public static List<TestSuite> getAllValidTestSuites() {
		List<TestSuite> ret = new ArrayList<RegressionTest.TestSuite>();
		ret.addAll(Arrays.asList(MANUAL_TESTS));
		
		for (String name : OfficialNASARegressionTests.EMPTY_SCRIPT_TESTS) {
			if (OfficialNASARegressionTests.BLACKLIST.contains(name)) {
				continue;
			}
			ret.add(produceEmptyScriptTest(name));
		}
		for (String name : OfficialNASARegressionTests.EMPTY_SCRIPT_VALID_TESTS) {
			if (OfficialNASARegressionTests.BLACKLIST.contains(name)) {
				continue;
			}
			ret.add(produceEmptyScriptTest(name));
		}
		for (Entry<String, Set<String>> e : OfficialNASARegressionTests.SAME_NAME_LIBRARY_TESTS.entrySet()) {
			if (OfficialNASARegressionTests.BLACKLIST.contains(e.getKey())) {
				continue;
			}
			ret.add(new TestSuite(e.getKey(), new String[]{e.getKey()}, 
					e.getValue().toArray(new String[]{})));
		}
		for (Entry<String, Set<String>> e : OfficialNASARegressionTests.EMPTY_SCRIPT_LIBRARY_TESTS.entrySet()) {
			if (OfficialNASARegressionTests.BLACKLIST.contains(e.getKey())) {
				continue;
			}
			ret.add(new TestSuite(e.getKey(), new String[]{}, 
					e.getValue().toArray(new String[]{})));
		}
		for (String name : OfficialNASARegressionTests.SAME_NAME_SCRIPT_TESTS) {
			if (OfficialNASARegressionTests.BLACKLIST.contains(name)) {
				continue;
			}
			ret.add(produceSameNameTest(name));
		}
		ret.add(new TestSuite("SimpleDriveRegressionTest", 
				OfficialNASARegressionTests.SIMPLE_DRIVE_SCRIPTS.toArray(new String[]{}),
				new String[]{}));
		
		return ret;
	}
	
	/**
	 * @return all of the TestSuites for Lustre regression testing. 
	 */
	public static List<TestSuite> getLustreTestSuites() {
		return getAllValidTestSuites().stream()
				.filter(ts -> ! OfficialNASARegressionTests.LUSTRE_BLACKLIST
							.contains(ts.planFile))
				.collect(Collectors.toList());
		
	}
	
	
	@Before
	public void turnOnVerbosity() {
		debugOn();
	}
	
	public static void debugOn() {
		JavaPlan.DEBUG = true;
		PlanState.DEBUG = true;
		ScriptedEnvironment.DEBUG = true;
	}
	
	public static void debugOff() {
		JavaPlan.DEBUG = false;
		PlanState.DEBUG = false;
		ScriptedEnvironment.DEBUG = false;
	}

	public void doSingleTest(String planName, String language) throws Exception {
		TestSuite found = getAllValidTestSuites().stream()
				.filter(suite -> suite.planFile.equals(planName))
				.findAny()
				.orElseThrow(() -> new RuntimeException(
						planName+" not found in list of tests!"));
		if (language.equalsIgnoreCase("IL")) { 
			runTestSuiteIL(found);
		} else if (language.equalsIgnoreCase("Lustre")) {
			runTestSuiteLustre(found);
		} else if (language.equalsIgnoreCase("Java")) {
			runTestSuiteJava(found);
		} else {
			throw new RuntimeException("What language is "+language+"?");
		}
		
	}
	
	@Test
	public void testSomethingSpecific() throws Exception {
		doSingleTest("lookup1", "il");
	}
	
	@Test
	public void testIL() throws Exception {
		for (TestSuite suite : getAllValidTestSuites()) {
			for (String script : suite.planScripts) {
				runSingleTestILSimulator(suite.planFile, script, suite.libs);
			}
		}
	}

	@Test
	public void testJava() throws Exception {
		for (TestSuite java : getAllValidTestSuites()) {
			for (String script : java.planScripts) {
				runSingleTestJava(java.planFile, script);
			}
		}
	}

	@Test
	public void testLustre() throws Exception {
		for (TestSuite lustre : getLustreTestSuites()) {
			for (String script : lustre.planScripts) {
				runSingleTestLustre(lustre.planFile, script, lustre.libs);
			}
		}
	}

	
//	public static List<PlanState> parseLogFile(String planName, String scriptName) throws Exception{
//		return parseLogFile(
//		        new File(ORACLE_LOGS, planName+"___"+scriptName+".log"));
//	}

	public static List<PlanState> parseLogFile(String planName, String scriptName) throws Exception{
		File plan = new File(RESOURCES, planName+".plx");
		Optional<File> script = Optional.empty();
		if (! scriptName.equals("empty")) {
			script = Optional.of(new File(RESOURCES, scriptName+".psx"));
		}
		File libDir = RESOURCES;
		return ComplianceTesting.generateOracle(plan, script, Optional.of(libDir));
	}

	
//	public static List<PlanState> parseLogFile(File logFile) throws Exception {
//		List<PlanState> oracles = new ArrayList<PlanState>();
//		BufferedReader in = new BufferedReader(new FileReader(logFile));
//		PlanState state = PlanState.parseLogFile(in);
//		while (state != null) {
//			if (PlanState.DEBUG) 
//				System.out.println("*******************************");
//			oracles.add(state);
//			state = PlanState.parseLogFile(in);
//		}
//		return oracles;
//	}
	

	public static JavaPlexilScript getScript(String scriptName) throws ReflectiveOperationException {
		if (scriptName.equals("empty")) {
		    return new JavaPlexilScript();
		} else {
			String cleanName = NameUtils.clean(scriptName);
		    Class<?> scriptClass = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+cleanName+"Script");
		    return (JavaPlexilScript) 
		        scriptClass.getConstructor().newInstance();
		}
	}
	
	public static JavaPlan getPlan(String planName, ExternalWorld world) throws ReflectiveOperationException {
		if (SIMULATE_ALL_JAVA_FILES) {
			// Give them an IL simulator instead. 
			TPlex exec = new TPlex();
			Plan p = exec.quickParsePlan(new File(RESOURCES, planName+".plx"));
//			System.out.println(p.printFullPlan());
			return new ILSimulator(p, world);
		}
		Class<?> main = Class.forName(TPLEX_OUTPUT_PACKAGE+"."+NameUtils.clean(planName));
		return (JavaPlan) main.getConstructor(ExternalWorld.class).newInstance(world);
	}
	
	public static void runSingleTestILSimulator(String planName, String scriptName, String[] libs) throws Exception {
		List<PlanState> expected = parseLogFile(planName, scriptName);
		
		assertTrue("Check the log file, nothing was pulled from it.", expected.size() > 0);
        System.out.println("Running "+planName+" with script "+scriptName);
        
		// Get the script
		JavaPlexilScript world = getScript(scriptName);
		// Read the plan
		Plan ilPlan = getSimulatablePlan(planName, libs);
		
		ComplianceTesting.intermediateLanguage(ilPlan, world, 
				new PlanStateChecker(expected));
	}
	
	private static Plan getSimulatablePlan(String planName, String[] libs) {
		TPlex tplex = new TPlex();
		tplex.staticLibraries = true;
		tplex.inferTypes = true;
		for (String library : libs) {
			File libFile = new File(RESOURCES, library+".plx");
			tplex.addToLibraryPath(libFile);
		}
		return tplex.quickParsePlan(new File(RESOURCES, planName+".plx"));
	}

	
	public static void runSingleTestJava(String planName, String scriptName) throws Exception {
		List<PlanState> expected = parseLogFile(planName, scriptName);
		
		assertTrue("Check the log file, nothing was pulled from it.", expected.size() > 0);

		
        System.out.println("Running "+planName+" with script "+scriptName);
        
//		root.fullReset();
		// Get the script
		JavaPlexilScript world = getScript(scriptName);
		// Need to find the root node
		JavaPlan root = getPlan(planName, world);
		
		ComplianceTesting.java(root, world, expected);
	}
	
	public static void runSingleTestLustre(String planName, String scriptName, String[] libs) throws Exception {
		// We're actually going to use the IL simulator to test the Lustre code. 
		// So first, make sure that the simulator matches the executive logs.
		runSingleTestILSimulator(planName, scriptName, libs);
		
		// Alright, the simulator runs this with no problems, so we can use it.
		JavaPlexilScript script = getScript(scriptName);
		Plan ilPlan = getSimulatablePlan(planName, libs);
		
		// And grab the Lustre stuff
		File lustreFile = new File(LUSTRE_FILES, planName+".lus");
		File csvFile = new File(LUSTRE_FILES, planName+"__"+scriptName+".csv");
		LustreTrace fullTrace = getFullTrace(lustreFile, planName, csvFile);
		ReverseTranslationMap mapper = TPlex.getStringMapForLus(lustreFile);
		
		complianceTest(ilPlan, script, fullTrace, mapper);
	}
	
	public static void complianceTest(Plan ilPlan, ExternalWorld environment,
			LustreTrace fullTrace, ReverseTranslationMap mapper) throws Exception{
		try {
			ComplianceTesting.lustreMatchesIL(ilPlan, environment, 
					ComplianceTesting.createLustreChecker(fullTrace, ilPlan, mapper));
		} catch (Exception e) {
			System.out.println("\n\n\n\n\n"+ScriptSimulation.toMoreReadableCSV(fullTrace));
			throw e;
		}
	}
	
	
	public static LustreTrace getFullTrace(File lustreFile, String mainNode, 
			File inputCsv) throws Exception{
		List<LustreTrace> ret = 
				JKindResultUtils.simulateCSV(lustreFile, mainNode, inputCsv);
		
		assertEquals("Only should have gotten 1 trace back", 1, ret.size());

		return ret.get(0);

	}

	public static void runTestSuiteIL(TestSuite suite) throws Exception {
	    for (String script : suite.planScripts) {
	        runSingleTestILSimulator(suite.planFile, script, suite.libs);
	    }
	}
	
	public static void runTestSuiteJava(TestSuite suite) throws Exception {
	    for (String script : suite.planScripts) {
	        runSingleTestJava(suite.planFile, script);
	    }
	}
	
	public static void runTestSuiteLustre(TestSuite suite) throws Exception {
		for (String script : suite.planScripts) {
			runSingleTestLustre(suite.planFile, script, suite.libs);
		}
	}
	
}
