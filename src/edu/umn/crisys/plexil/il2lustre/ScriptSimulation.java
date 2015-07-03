package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jkind.lustre.visitors.PrettyPrintVisitor;
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
			// Need to make sure we're getting raw input names
			if (expr instanceof LookupExpr) {
				// Get the raw input name
				line.add(LustreNamingConventions.getRawLookupId(
						((LookupExpr) expr).getLookupNameAsString()));
			} else if (expr instanceof ILVariable) {
				// These should be command handles
				line.add(LustreNamingConventions
						.getRawCommandHandleId((ILVariable) expr));
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
			
			/**
			 * We want to capture the initial state before the first micro
			 * step is committed. After that, we want a capture between macro
			 * steps because Lustre takes a step to let that happen. 
			 */
			private boolean captureBeforeCommit = true;
			
			@Override
			public void endOfMicroStepBeforeCommit(JavaPlan plan) {
				// This is our only shot at the initial state of the plan. 
				// After that, we want the post-commit values of everything.
				if (captureBeforeCommit) {
					captureState(plan);
					captureBeforeCommit = false;
				}
			}
			
			@Override
			public void endOfMicroStepAfterCommit(JavaPlan plan) {
				// Capture every state after it gets committed
				captureState(plan);
			}
			
			@Override
			public void endOfMacroStep(JavaPlan plan) {
				// We just ended a macro step. Before we commit the next one,
				// capture the inbetween state.
				captureBeforeCommit = true;
			}
			
			private void captureState(JavaPlan plan) {
				for ( Entry<Expression, List<PValue>> e : csv.entrySet()) {
					e.getValue().add(sim.eval(e.getKey()));
				}
			}
		});
		
		// Now run the simulation
		sim.runPlanToCompletion();
		
		// All done!
		return csv;
	}
	
}
