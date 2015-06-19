package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.io.PrintStream;

import edu.umn.crisys.plexil.test.java.RegressionTest.TestSuite;

public class GenerateOracleScript {
    
    

    /**
     * Create the script to generate .log files. Here's what to do:
     * 
     * 1: Run this, it generates generate_logs.sh
     * 2: Run generate_logs.sh on a system where Plexil works
     * 
     * Now regression tests will be based on fresh new logs.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream("generate_logs.sh");
        out.println("#!/bin/sh");
        
        // Run complex tests:
        for (TestSuite suite : RegressionTest.getJavaTestSuites()) {
        	if (suite.planScripts.length == 0) {
        		printRun(out, suite.planFile, "empty");
        	} else {
        		for (String script : suite.planScripts) {
        			printRun(out, suite.planFile, script);
        		}
        	}
        }
    }
    
    private static void printRun(PrintStream out, String plan, 
            String script) {
        File debugConfig = new File(RegressionTest.RESOURCES, "Debug.cfg");
        File planFile = new File(RegressionTest.RESOURCES, plan+".plx");
        File scriptFile = new File(RegressionTest.RESOURCES, script+".psx");
        File libraryLocation = RegressionTest.RESOURCES;
        File logDestination = new File(RegressionTest.ORACLE_LOGS, plan+"___"+script+".log");
        
        if ( ! debugConfig.isFile()) {
        	throw new RuntimeException("Debug.cfg does not exist");
        }
        if ( ! planFile.isFile()) {
        	throw new RuntimeException("Plan "+plan+" does not exist at "+planFile.getPath());
        }
        if ( ! scriptFile.isFile()) {
        	throw new RuntimeException("Plan "+script+" does not exist at "+scriptFile.getPath());
        }
        
        out.println("echo Plan "+plan+", script "+script);
        out.println("plexiltest -q -d "+debugConfig.getPath()
        		+" -p "+planFile.getPath()
        		+" -s "+scriptFile.getPath()
                +" -L "+libraryLocation.getPath()
                +" > "+logDestination.getPath());
        
    }

}
