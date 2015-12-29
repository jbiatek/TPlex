package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jkind.lustre.visitors.PrettyPrintVisitor;
import lustre.LustreTrace;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.script.ast.PlexilScript;

public class ScriptSimulation {
	
	public static boolean DEBUG = false;


	public static String toLustreCSV(LinkedHashMap<Expression,List<PValue>> data,
			ReverseTranslationMap stringMap) {
		// Check that each list of values is the same length
		int size = data.entrySet().stream()
			.mapToInt(entry -> entry.getValue().size())
			.reduce((a,b) -> a == b ? a : -1)
			.orElse(0);

		if (size == -1) {
			throw new RuntimeException("CSV data was not all the same length!");
		}
		
		// Print the headers first
		List<String> line = new ArrayList<String>();
		for (Expression expr : data.keySet()) {
			if (expr instanceof LookupExpr) {
				// Get the raw input name
				line.add(LustreNamingConventions.getLookupId(
						((LookupExpr) expr).getLookupNameAsString()));
			} else if (expr instanceof ILVariable) {
				// These should be command handles
				line.add(LustreNamingConventions
						.getVariableId((ILVariable) expr));
			}
		}
		// Print that first line
		StringBuilder csv = new StringBuilder();
		csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		
		// For the rest of the data, we go step by step through the lists
		ILExprToLustre exprToLustre = new ILExprToLustre(stringMap);
		for (int i = 0; i < size; i++) {
			line.clear();
			for (List<PValue> list : data.values()) {
				PrettyPrintVisitor pp = new PrettyPrintVisitor();
				// Translate to Lustre, then 
				list.get(i).accept(exprToLustre, null)
						.accept(pp);
				line.add(pp.toString());
			}
			csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		}
		
		return csv.toString();
	}
	
	public static String toCSV(LustreTrace trace) {
		LinkedHashMap<String,List<String>> data = new LinkedHashMap<>();
		for (String name : trace.getVariableNames()) {
			List<String> values = new ArrayList<>();
			for (int i = 0; i < trace.getLength(); i++) {
				String value = "null";
				if (trace.getVariable(name).getValue(i) != null) {
					value = trace.getVariable(name).getValue(i).toString();
				}
				
				values.add(value);
			}
			data.put(name, values);
		}
		return toCSV(data);
	}
	
	public static String toCSV(LinkedHashMap<String, List<String>> data) {
		// Check that each list of values is the same length
		int size = data.entrySet().stream()
			.mapToInt(entry -> entry.getValue().size())
			.reduce((a,b) -> a == b ? a : -1)
			.orElse(0);

		if (size == -1) {
			throw new RuntimeException("CSV data was not all the same length!");
		}
		
		// Print the headers first
		List<String> line = new ArrayList<String>();
		line.addAll(data.keySet());

		// Print that first line
		StringBuilder csv = new StringBuilder();
		csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		
		// For the rest of the data, we go step by step through the lists
		for (int i = 0; i < size; i++) {
			line.clear();
			for (List<String> list : data.values()) {
				line.add(list.get(i));
			}
			csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		}
		
		return csv.toString();
	}
	

	public static LinkedHashMap<Expression,List<PValue>> 
	simulateToCSV(Plan ilPlan, PlexilScript astScript) {
		JavaPlexilScript script = new JavaPlexilScript(astScript);
		ILSimulator sim = new ILSimulator(ilPlan, script);

		// LinkedHashMap guarantees iteration order, so we want that specifically
		LinkedHashMap<Expression, List<PValue>> csv = new LinkedHashMap<>();
		
		// Initialize all the keys we expect to see
		for (LookupDecl lookup : ilPlan.getStateDecls()) {
			if (lookup.getParameters().size() != 0) {
				System.err.println("Warning: lookups with parameters aren't supported!");
			}
			csv.put(new LookupNowExpr(lookup.getName()), new ArrayList<>());
		}
		// Need to find all the command handles too
		for (ILVariable var : ilPlan.getVariables()) {
			if (var instanceof SimpleVar) {
				SimpleVar simple = (SimpleVar) var;
				if (simple.getType().equals(ExprType.COMMAND_HANDLE)) {
					csv.put(simple,	new ArrayList<>());
				}
			}
		}
		
		sim.addObserver(new JavaPlanObserver() {
			
			private boolean captureStartOfNextMicroStepInstead = false;

			@Override
			public void beforePlanExecution(JavaPlan plan) {
				// We want the initial state of the plan as our first entry.
				if (DEBUG) {
					System.out.println("Capturing initial plan state");
				}
				captureState(plan);
			}

			@Override
			public void endOfMicroStepAfterCommit(JavaPlan plan) {
				// Normally, we just capture every micro step after it gets 
				// committed...
				if (DEBUG) {
					System.out.println("Capturing after micro step was committed");
				}
				captureState(plan);
			}

			@Override
			public void endOfMacroStep(JavaPlan plan) {
				// ...However, inputs change between macro steps. In Lustre, 
				// that means those changes have to become visible in the last 
				// micro step before the new macro step begins. Whatever we just
				// captured in endOfMicroStepAfterCommit() might change, so
				// we'll just delete it. 
				if (DEBUG) {
					System.out.println("Macro step ended! Deleting last state, will capture before the next step commits. ");
				}
				deleteLastState();
				captureStartOfNextMicroStepInstead = true;
			}

			@Override
			public void endOfMicroStepBeforeCommit(JavaPlan plan) {
				if (captureStartOfNextMicroStepInstead) {
					// Now that we're starting the next macro step, capture
					// pre-commit values. That way, we're recording the same 
					// new inputs that the plan saw.
					if (DEBUG) {
						System.out.println("Replacing deleted state before first micro step is committed");
					}
					captureState(plan);
					captureStartOfNextMicroStepInstead = false;
				}
			}

			private void captureState(JavaPlan plan) {
				for ( Entry<Expression, List<PValue>> e : csv.entrySet()) {
					e.getValue().add(sim.eval(e.getKey()));
				}
			}
			
			private void deleteLastState() {
				for (List<PValue> values : csv.values()) {
					values.remove(values.size()-1);
				}
			}
		});
		
		// Now run the simulation
		sim.runPlanToCompletion();
		
		// All done!
		return csv;
	}
	
}
