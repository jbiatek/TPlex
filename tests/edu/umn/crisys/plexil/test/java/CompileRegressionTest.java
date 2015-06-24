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
		for (TestSuite suite : RegressionTest.getJavaTestSuites()) {
		    buildTestJava(suite, RegressionTest.RESOURCES, RegressionTest.TESTING_DIRECTORY);
		}
		for (TestSuite suite : RegressionTest.getLustreTestSuites()) {
			buildTestLustre(suite, RegressionTest.RESOURCES, RegressionTest.LUSTRE_FILES);
		}
	}
	
	private static void buildTestJava(TestSuite suite, File resources, File outputDir) throws Exception {
		TPlex tplex = new TPlex();
		tplex.outputLanguage = OutputLanguage.JAVA;
		tplex.outputDir = outputDir;
		tplex.javaPackage = RegressionTest.TPLEX_OUTPUT_PACKAGE;
		
		tplex.files.add(new File(resources, suite.planFile+".plx"));
		for (String scriptName : suite.planScripts) {
			tplex.files.add(new File(resources, scriptName+".psx"));
		}
		for (String libName : suite.libs) {
			tplex.files.add(new File(resources, libName+".plx"));
		}

		tplex.execute();
	}
	
	private static void buildTestLustre(TestSuite suite, File resources, File outputDir) throws Exception {
		TPlex tplex = new TPlex();
		tplex.outputLanguage = OutputLanguage.LUSTRE;
		tplex.outputDir = outputDir;
		tplex.lustreSimulateScriptsAgainst = suite.planFile;
		
		tplex.files.add(new File(resources, suite.planFile+".plx"));
		
		for (String scriptName : suite.planScripts) {
			tplex.files.add(new File(resources, scriptName+".psx"));
		}
		for (String libName : suite.libs) {
			throw new RuntimeException("Libraries not supported in Lustre testing yet");
			//args.add(new File(resources, libName+".plx").getPath());
		}
		
		tplex.execute();
	}

}
