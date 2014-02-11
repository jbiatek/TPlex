package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.ast.core.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.NodeToIL;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.psx2java.PsxParser;
import edu.umn.crisys.plexil.test.java.RegressionTest.TestSuite;
import edu.umn.crisys.plexil.translator.il.Plan;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

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
	
	private static void buildLibrary(String name, File resources, File dest) throws Exception {
        System.out.println("Compiling "+name);
        PlexilPlan planXml = PlxParser.parseFile(new File(resources, name+".plx"));
        NodeToIL rootTranslator = new NodeToIL(planXml.getRootNode());
        Plan ilPlan = new Plan(name);
        rootTranslator.translate(ilPlan);
        
        JCodeModel cm = new JCodeModel();
        String pkg = "generated";
        
        JDefinedClass javaCode = ilPlan.toJava(cm, pkg, true);
        
        addGetSnapshotMethod(rootTranslator, javaCode);
        cm.build(dest);

	}
	
	private static void buildTest(TestSuite suite, File resources, File dest) throws Exception {
	    System.out.println("Compiling "+suite.planFile);
        PlexilPlan planXml = PlxParser.parseFile(new File(resources, suite.planFile+".plx"));
        NodeToIL translator = new NodeToIL(planXml.getRootNode());
        Plan ilPlan = new Plan(suite.planFile);
        translator.translate(ilPlan);
        
        JCodeModel cm = new JCodeModel();
        String pkg = "generated";
        
        JDefinedClass javaCode = ilPlan.toJava(cm, pkg, false);
        
        addGetSnapshotMethod(translator, javaCode);
        
        XMLInputFactory factory = XMLInputFactory.newFactory();
        
        for (String scriptName : suite.planScripts) {
            System.out.println("Compiling script "+scriptName);
            XMLEventReader xml = factory.createXMLEventReader(
                    new FileInputStream(new File(resources, scriptName+".psx")));
            
            String name = scriptName.replaceAll("-", "_") + "Script";
            PsxParser.parse(name, xml).toJava(cm, pkg);
        }
        
        cm.build(dest);
	}

    public static void addGetSnapshotMethod(NodeToIL translator,
            JDefinedClass javaCode) {
        
        javaCode._implements(PlexilTestable.class);
        
        JCodeModel cm = javaCode.owner();
        JMethod m = javaCode.method(JMod.PUBLIC, cm.ref(PlanState.class), "getSnapshot");
        
        // For every Plexil node, we need to grab its variables and put them
        // into a PlanState node.
        
        JVar root = addSnapshotInfo(translator, null, m.body(), cm);
        
        m.body()._return(root);
        
    }
    
    /**
     * Add Java code to the given JBlock that creates a local PlanState 
     * variable and fills it in with the correct variable and child information. 
     * @param translator The node to capture the info of
     * @param parent The parent PlanState var, or null if it's the root.
     * @param b The block of code to add to.
     * @param cm
     * @return the local variable that was made.
     */
    private static JVar addSnapshotInfo(NodeToIL translator, JVar parent, JBlock b,
            JCodeModel cm) {
        JInvocation planStateInit = JExpr._new(cm.ref(PlanState.class))
            .arg(translator.getUID().getShortName());
        if (parent != null) {
            planStateInit.arg(parent);
        } 
        JVar ps = b.decl(cm.ref(PlanState.class), translator.getUID().toCleanString(), planStateInit);
        
        for (String varName : translator.getAllVariables()) {
            IntermediateVariable v = translator.getVariable(varName);
            
            if (v.getNameForTesting() == null) continue;
            b.invoke(ps, "addVariable").arg(v.getNameForTesting()).arg(v.rhs(cm));
        }
        
        for (NodeToIL child : translator.getChildren()) {
            JVar childVar = addSnapshotInfo(child, ps, b, cm);
            b.invoke(ps, "addChild").arg(childVar);
        }
        
        if (translator.hasLibraryHandle()) {
            b.invoke(ps, "addChild").arg(
                    JExpr.invoke(JExpr.cast(cm.ref(PlexilTestable.class), 
                    translator.getLibraryHandle().directReference(cm)), "getSnapshot"));

        }
        
        return ps;
        
    }

}
