package edu.umn.crisys.plexil.test.java;

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
        for (TestSuite suite : RegressionTest.TESTS) {
            for (String script : suite.planScripts) {
                printRun(out, suite.planFile, script);
            }
        }
        
        // Compile simple tests, where the script and plan have the same name
        for (String name : RegressionTest.SAME_NAME_TESTS) {
            printRun(out, name, name);
        }
        
        // Compile empty tests, where the script doesn't even need to be there
        for (String name : RegressionTest.EMPTY_SCRIPT_TESTS) {
            printRun(out, name, "empty.psx");
        }
    }
    
    private static void printRun(PrintStream out, String planFile, 
            String scriptFile) {
        String path = "tests/edu/umn/crisys/plexil/test/resources/";
        String planName = planFile.replaceFirst(".plx$", "");
        String scriptName = scriptFile.replaceFirst(".psx$", "");

        out.println("echo Plan "+planName+", script "+scriptName);
        out.println("plexiltest -q -p "+path+planName+".plx -s "+path+scriptName
                +".psx -L "+path+" > "+path+planName+"___"+scriptName+".log");
        
    }

}
