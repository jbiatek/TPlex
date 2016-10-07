package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jkind.lustre.Expr;
import jkind.lustre.visitors.PrettyPrintVisitor;
import lustre.LustreTrace;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.LookupExpr;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.script.ast.PlexilScript;

public class ScriptSimulation {
	
	public static boolean DEBUG = false;


	public static String toLustreCSV(LinkedHashMap<ILExpr,List<PValue>> data,
			ReverseTranslationMap stringMap) {
		// Check that each list of values is the same length
		int size = data.entrySet().stream()
			.mapToInt(entry -> entry.getValue().size())
			.reduce((a,b) -> a == b ? a : -1)
			.orElse(-1);

		if (size == -1) {
			throw new RuntimeException("CSV data was not all the same length!");
		}
		
		// Print the headers first
		List<String> line = new ArrayList<String>();
		for (ILExpr expr : data.keySet()) {
			if (expr instanceof LookupExpr) {
				// Check for a value/known split
				if (LustreNamingConventions.hasValueAndKnownSplit(expr.getType())) {
					line.add(LustreNamingConventions.getRawLookupIdValuePart(((LookupExpr) expr)));
					line.add(LustreNamingConventions.getRawLookupIdKnownPart(((LookupExpr) expr)));
				} else {
					// Get the raw input name
					line.add(LustreNamingConventions.getRawLookupId(
							((LookupExpr) expr)));
				}
			} else if (expr instanceof ILVariable) {
				// Value/known split here too
				if (LustreNamingConventions.hasValueAndKnownSplit(expr.getType())) {
					line.add(LustreNamingConventions.getRawInputIdValuePart(((ILVariable) expr)));
					line.add(LustreNamingConventions.getRawInputIdKnownPart(((ILVariable) expr)));
				} else {
					// Get the raw input name
					line.add(LustreNamingConventions.getRawInputId(
							((ILVariable) expr)));
				}
			}
		}
		// Print that first line
		StringBuilder csv = new StringBuilder();
		csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		
		// For the rest of the data, we go step by step through the lists
		ILExprToLustre exprToLustre = new ILExprToLustre(stringMap);
		for (int i = 0; i < size; i++) {
			line.clear();
			for (Entry<ILExpr, List<PValue>> e : data.entrySet()) {
				List<PValue> list = e.getValue();
				// Needs to be translated to Lustre first
				if (LustreNamingConventions.hasValueAndKnownSplit(
						e.getKey().getType())) {
					// Split these up into separate entries!
					PrettyPrintVisitor valuePrinter = new PrettyPrintVisitor();
					PrettyPrintVisitor knownPrinter = new PrettyPrintVisitor();
					Expr tuple = list.get(i).accept(exprToLustre, e.getKey().getType());
					ILExprToLustre.getValueComponent(tuple).accept(valuePrinter);
					ILExprToLustre.getKnownComponent(tuple).accept(knownPrinter);
					line.add(valuePrinter.toString());
					line.add(knownPrinter.toString());
				} else {
					PrettyPrintVisitor pp = new PrettyPrintVisitor();
					// Simple translation, easy peasy
					list.get(i).accept(exprToLustre, e.getKey().getType())
							.accept(pp);
					line.add(pp.toString());
				}
			}
			csv.append(line.stream().collect(Collectors.joining(",", "", "\n")));
		}
		
		return csv.toString();
	}
	
	public static String toCSV(LustreTrace trace) {
		return toCSV(traceToMap(trace, Optional.empty()));
	}
	
	public static String toMoreReadableCSV(LustreTrace trace) {
		return toCSV(traceToMap(trace, Optional.of(name -> ! name.contains("~"))));
	}
	
	/**
	 * Turn a trace into a LinkedHashMap of values. If you want, you can also
	 * provide a filtering function, which takes the name of a variable and
	 * returns true if it should be included in the map. (If one isn't provided,
	 * all variables will be let in.)
	 * 
	 * @param trace
	 * @param filter
	 * @return
	 */
	private static LinkedHashMap<String,List<String>> traceToMap(LustreTrace trace,
			Optional<Function<String,Boolean>> filter) {
		LinkedHashMap<String,List<String>> data = new LinkedHashMap<>();
		for (String name : trace.getVariableNames()) {
			if (filter.map(f -> f.apply(name)).orElse(true)) {
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
		}
		return data;
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
	

	public static LinkedHashMap<ILExpr,List<PValue>> 
	simulateToCSV(Plan ilPlan, PlexilScript astScript) {
		JavaPlexilScript script = new JavaPlexilScript(astScript);
		ILSimulator sim = new ILSimulator(ilPlan, script);

		// LinkedHashMap guarantees iteration order, so we want that specifically
		LinkedHashMap<ILExpr, List<PValue>> csv = new LinkedHashMap<>();
		
		// Initialize all the keys we expect to see
		for (LookupDecl lookup : ilPlan.getStateDecls()) {
			if (lookup.getParameters().size() != 0) {
				System.err.println("Warning: lookups with parameters aren't supported!");
			}
			csv.put(new LookupExpr(
					lookup,
					StringValue.get(lookup.getName()), Collections.emptyList()), 
					
					new ArrayList<>());
		}
		// Need to find all the command handles too
		for (ILVariable var : ilPlan.getVariables()) {
			if (var instanceof SimpleVar) {
				SimpleVar simple = (SimpleVar) var;
				if (simple.getType().equals(ILType.COMMAND_HANDLE)) {
					csv.put(simple,	new ArrayList<>());
				}
			}
		}
		
		// Tell the script to handle built-in PLEXIL commands like print and
		// pprint:
		script.getEnvironment().enableAutoResponseToPlexilPrintCommands();
		script.getEnvironment().enableExceptionsForMissingLookupData();
		
		sim.addObserver(new JavaPlanObserver() {


			@Override
			public void beforeMicroStepRuns(JavaPlan plan) {
				// Capture all values just before a new micro step begins.
				// This corresponds with what Lustre does in practice,
				// and more importantly it incorporates the fact that in Lustre,
				// inputs actually appear to change "early" so that they are 
				// visible in the next macro step.
				captureState(plan);
			}

			private void captureState(JavaPlan plan) {
				for ( Entry<ILExpr, List<PValue>> e : csv.entrySet()) {
					e.getValue().add(sim.eval(e.getKey()));
				}
			}
			
		});
		
		// Now run the simulation
		sim.runPlanUntilEnvironmentDone();
		
		// All done!
		return csv;
	}
	
}
