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
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.optimize.PruneUnusedTimepoints;
import edu.umn.crisys.plexil.il.optimize.RemoveDeadTransitions;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.PlanToJava;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.plx2ast.PlxParser;
import edu.umn.crisys.plexil.psx2java.PsxParser;
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
        
        addGetSnapshotMethod(ilPlan, rootTranslator, javaCode);
        cm.build(dest);

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
        
        JDefinedClass javaCode = PlanToJava.toJava(ilPlan, cm, pkg, null);
        
        addGetSnapshotMethod(ilPlan, translator, javaCode);
        
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

    public static void addGetSnapshotMethod(Plan ilPlan, NodeToIL translator,
            JDefinedClass javaCode) {
        
        javaCode._implements(PlexilTestable.class);
        
        JCodeModel cm = javaCode.owner();
        JMethod m = javaCode.method(JMod.PUBLIC, cm.ref(PlanState.class), "getSnapshot");
        
        // For every Plexil node, we need to grab its variables and put them
        // into a PlanState node.
        
        JVar root = addSnapshotInfo(ilPlan, translator, null, m.body(), cm);
        
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
    private static JVar addSnapshotInfo(Plan ilPlan, NodeToIL translator, JVar parent, JBlock b,
            JCodeModel cm) {
        JInvocation planStateInit = JExpr._new(cm.ref(PlanState.class))
            .arg(translator.getUID().getShortName());
        if (parent != null) {
            planStateInit.arg(parent);
        } 
        JVar ps = b.decl(cm.ref(PlanState.class), translator.getUID().toCleanString(), planStateInit);
        
        for (String varName : translator.getAllVariables()) {
            ILVariable v = translator.getVariable(varName);
            if ( ! ilPlan.getVariables().contains(v)) {
            	// This variable didn't make it into the final version.
            	continue;
            }
            
            
            String name = v.getName();
            
            if (name.startsWith(".")) {
            	// Internal variables are named "ShortName.command_handle", etc.
            	name = v.getNodeUID().getShortName()+name;
            }
            
            b.invoke(ps, "addVariable").arg(name).arg(ILExprToJava.toJava(v, cm));
        }
        
        for (NodeToIL child : translator.getChildren()) {
            JVar childVar = addSnapshotInfo(ilPlan, child, ps, b, cm);
            b.invoke(ps, "addChild").arg(childVar);
        }
        
        if (translator.hasLibraryHandle()) {
        	// We need direct access to the field here.
            b.invoke(ps, "addChild").arg(
                    JExpr.invoke(JExpr.cast(cm.ref(PlexilTestable.class), 
                    JExpr.ref(ILExprToJava.getLibraryFieldName(translator.getUID()))), "getSnapshot"));

        }
        
        return ps;
        
    }

}
