package edu.umn.crisys.plexil.test.java;

import java.io.File;

import edu.umn.crisys.plexil.main.TPlex;
import edu.umn.crisys.plexil.main.TPlex.OutputLanguage;
import edu.umn.crisys.plexil.test.java.RegressionTest.TestSuite;

public class CompileRegressionTest {
    
	/**
	 * Directions: 
	 * 		Run this to generate Java code to tests/
	 * 		In Eclipse, refresh the project with F5 because files are now out
	 * 			of sync, or compile the generated code some other way
	 * 		Run RegressionTest in JUnit
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Compile tests:
		/*/
		buildTestLustre(getSuiteByName("boolean1"),
				RegressionTest.RESOURCES,
				RegressionTest.LUSTRE_FILES);
		//*/
		
		for (TestSuite suite : RegressionTest.getAllValidTestSuites()) {
		    buildTestJava(suite, RegressionTest.RESOURCES, RegressionTest.TESTING_DIRECTORY);
		}
		for (TestSuite suite : RegressionTest.getLustreTestSuites()) {
			buildTestLustre(suite, RegressionTest.RESOURCES, RegressionTest.LUSTRE_FILES);
		}
		//*/
	}
	
	private static TestSuite getSuiteByName(String name) {
		return RegressionTest.getAllValidTestSuites()
				.stream()
				.filter(t -> t.planFile.equals(name))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Test plan "+name+" not found"));

	}
	
	private static void buildTestGeneric(TPlex preconfigured, TestSuite suite,
			File resources, File outputDir) throws Exception {
		preconfigured.outputDir = outputDir;
		preconfigured.inferTypes = true;
		
		preconfigured.files.add(new File(resources, suite.planFile+".plx"));
		for (String scriptName : suite.planScripts) {
			preconfigured.files.add(new File(resources, scriptName+".psx"));
		}

		try {
			if ( ! preconfigured.execute()) {
				throw new RuntimeException("TPlex didn't execute cleanly!");
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem compiling "+suite.planFile, e);
		}

	}
	
	private static void buildTestJava(TestSuite suite, File resources, File outputDir) throws Exception {
		TPlex preconfigured = new TPlex();
		preconfigured.outputLanguage = OutputLanguage.JAVA;
		preconfigured.javaPackage = RegressionTest.TPLEX_OUTPUT_PACKAGE;

		for (String libName : suite.libs) {
			preconfigured.files.add(new File(resources, libName+".plx"));
		}
		
		buildTestGeneric(preconfigured, suite, resources, outputDir);
	}
	
	private static void buildTestLustre(TestSuite suite, File resources, File outputDir) throws Exception {
		TPlex preconfigured = new TPlex();
		preconfigured.outputLanguage = OutputLanguage.LUSTRE;
		preconfigured.lustreSimulateScriptsAgainst = suite.planFile;
		preconfigured.staticLibraries = true;
		
		for (String libName : suite.libs) {
			preconfigured.addToLibraryPath(new File(resources, libName+".plx"));
		}
		
		buildTestGeneric(preconfigured, suite, resources, outputDir);
	}

}
