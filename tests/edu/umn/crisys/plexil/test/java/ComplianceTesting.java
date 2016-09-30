package edu.umn.crisys.plexil.test.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.PlanState;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.translator.ScriptToXML;
import jkind.lustre.values.Value;
import jkind.results.Signal;
import lustre.LustreTrace;

public class ComplianceTesting {

	public static List<PlanState> generateOracle(File plan, Optional<File> script, Optional<File> libDir) 
	throws IOException {
		BufferedReader in = new OfficialPlexilExecutive(plan)
				.setScript(script)
				.setLibDir(libDir)
				.setDebugFile(new File(RegressionTest.RESOURCES, "Debug.cfg"))
				.generateLog();
		return PlanState.parseLogFile(in);
	}

	public static List<PlanState> generateOracle(File plan, Optional<File> script, List<File> libs)
	throws IOException {
		OfficialPlexilExecutive exec = new OfficialPlexilExecutive(plan);
		exec.setScript(script);
		libs.forEach(lib -> exec.addLibrary(lib));
		return PlanState.parseLogFile(exec.generateLog());
	}
	
	
	public static LustreComplianceChecker createLustreChecker(
			LustreTrace fullTrace, Plan ilPlan, ReverseTranslationMap mapper) {
		final Map<ILExpr, List<PValue>> ilTrace = 
				JKindResultUtils.createILMapFromLustre(fullTrace, ilPlan, mapper);
		// This isn't an IL variable, it's PLEXIL semantics encoded in Lustre. 
		// If it's wrong, we want to know so that we can debug it, because it'll
		// mess up other stuff too. 
		final Signal<Value> macrostepEnded = 
				fullTrace.getVariable(LustreNamingConventions.MACRO_STEP_ENDED_ID);
		
		return new LustreComplianceChecker(ilTrace, macrostepEnded, mapper);

	}

	public static File writeScriptToTempFile(PlexilScript script) {
		try {
			File temp = File.createTempFile(script.getScriptName(), ".psx");
			ScriptToXML.writeToFile(temp, script);
			return temp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File createDebugCfgFile() {
		try {
			File debugFile = File.createTempFile("Debug", ".cfg");
			PrintStream out = new PrintStream(debugFile);
			out.println(":PlexilExec:printPlan");
			out.close();
			return debugFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void java(JavaPlan plan, ExternalWorld script, List<PlanState> oracle) {
		java(plan, script, new PlanStateChecker(oracle));
	}
	
	public static void java(JavaPlan plan, ExternalWorld script, TestOracle oracle) {
		plan.addObserver(oracle);
		if (script instanceof JavaPlexilScript) {
			((JavaPlexilScript) script).reset();
		}
		plan.setWorld(script);
		plan.runPlanToCompletion();
	}
	
	public static void intermediateLanguage(Plan ilPlan, ExternalWorld script, TestOracle oracle) {
		java(new ILSimulator(ilPlan, script), script, oracle);
	}
	
	public static void lustre(Plan ilPlan, ExternalWorld script, 
			List<PlanState> fromExecutive, LustreComplianceChecker fromTrace) {
		// Is the IL representing this correctly?
		intermediateLanguage(ilPlan, script, new PlanStateChecker(fromExecutive));
		// Does Lustre match the IL?
		lustreMatchesIL(ilPlan, script, fromTrace);
	}
	
	public static void lustreMatchesIL(Plan ilPlan, ExternalWorld script, LustreComplianceChecker checker) {
		intermediateLanguage(ilPlan, script, checker);
	}
	
}
