package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.script.translator.ScriptParser;
import edu.umn.crisys.plexil.script.translator.ScriptToJava;
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
		File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
	      File dest = new File("tests");

		
		for (String library : RegressionTest.libPath) {
		    buildLibrary(library, resources, dest);
		}
		
		// Compile complex tests:
		for (TestSuite suite : RegressionTest.TESTS) {
		    buildTest(suite, resources, dest);
		}
		
		// Compile simple tests, where the script and plan have the same name
		for (String name : RegressionTest.SAME_NAME_TESTS) {
		    TestSuite suite = new TestSuite(name, new String[] { name }, RegressionTest.libPath);
		    buildTest(suite, resources, dest);
		}
		
		// Compile empty tests, where the script doesn't even need to be there
		for (String name : RegressionTest.EMPTY_SCRIPT_TESTS) {
		    TestSuite suite = new TestSuite(name, new String[] { }, RegressionTest.libPath);
            buildTest(suite, resources, dest);
		}
	}
	
	/*
	private static void buildLibrary(String name, File resources, File dest) throws Exception {
		// TODO: This is almost exactly the same as below. WTF?
        System.out.println("Compiling "+name);
        PlexilPlan planXml = PlxParser.parseFile(new File(resources, name+".plx"));
        NodeToIL rootTranslator = new NodeToIL(planXml.getRootNode());
        Plan ilPlan = new Plan(name);
        rootTranslator.translate(ilPlan);
        
        PruneUnusedTimepoints.optimize(ilPlan);
        RemoveDeadTransitions.optimize(ilPlan);
        
        JCodeModel cm = new JCodeModel();
        String pkg = "generated";
        
        JDefinedClass javaCode = PlanToJava.toJava(ilPlan, cm, pkg, null);
        
        PlanToJava.addGetSnapshotMethod(ilPlan, rootTranslator, javaCode);
        cm.build(dest);

	}*/
	
	private static void buildLibrary(String name, File resources, File dest) throws Exception {
		buildTest(new TestSuite(name, new String[]{}, new String[]{}), resources, dest);
	}
	
	private static void buildTest(TestSuite suite, File resources, File dest) throws Exception {
	    System.out.println("Compiling "+suite.planFile);
        PlexilPlan planXml = PlxParser.parseFile(new File(resources, suite.planFile+".plx"));
        NodeToIL translator = new NodeToIL(planXml.getRootNode());
        Plan ilPlan = new Plan(suite.planFile);
        translator.translate(ilPlan);
        
        PruneUnusedTimepoints.optimize(ilPlan);
        RemoveDeadTransitions.optimize(ilPlan);

        
        JCodeModel cm = new JCodeModel();
        String pkg = "generated";
        
        JDefinedClass javaCode = PlanToJava.toJava(ilPlan, cm, pkg, true, null);
        
        PlanToJava.addGetSnapshotMethod(ilPlan, translator, javaCode);
        
        XMLInputFactory factory = XMLInputFactory.newFactory();
        
        for (String scriptName : suite.planScripts) {
            System.out.println("Compiling script "+scriptName);
            XMLEventReader xml = factory.createXMLEventReader(
                    new FileInputStream(new File(resources, scriptName+".psx")));
            
            String name = scriptName.replaceAll("-", "_") + "Script";
            ScriptToJava.toJava(ScriptParser.parse(name, xml), cm, pkg);
        }
        
        cm.build(dest);
	}

}
