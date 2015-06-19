package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
		List<String> args = new ArrayList<String>();
		args.add("--lang"); args.add("java");
		args.add("--output-dir"); args.add(outputDir.getPath());
		args.add("--java-package"); args.add(RegressionTest.TPLEX_OUTPUT_PACKAGE);
		
		args.add(new File(resources, suite.planFile+".plx").getPath());
		
		for (String scriptName : suite.planScripts) {
			args.add(new File(resources, scriptName+".psx").getPath());
		}
		for (String libName : suite.libs) {
			args.add(new File(resources, libName+".plx").getPath());
		}
		
		edu.umn.crisys.plexil.main.Main.main(args.toArray(new String[]{}));
	}
	
	private static void buildTestLustre(TestSuite suite, File resources, File outputDir) throws Exception {
		List<String> args = new ArrayList<String>();
		args.add("--lang"); args.add("lustre");
		args.add("--output-dir"); args.add(outputDir.getPath());
		
		args.add(new File(resources, suite.planFile+".plx").getPath());
		
		edu.umn.crisys.plexil.main.Main.main(args.toArray(new String[]{}));

		
		
		for (String scriptName : suite.planScripts) {
			throw new RuntimeException("Scripts need to be translated to CSVs for simulation");
			//args.add(new File(resources, scriptName+".psx").getPath());
		}
		for (String libName : suite.libs) {
			throw new RuntimeException("Libraries not supported in Lustre testing yet");
			//args.add(new File(resources, libName+".plx").getPath());
		}
		
	}

}
