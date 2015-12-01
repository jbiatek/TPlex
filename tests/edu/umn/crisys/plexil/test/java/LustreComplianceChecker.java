package edu.umn.crisys.plexil.test.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jkind.lustre.values.Value;
import jkind.results.Signal;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class LustreComplianceChecker extends TestOracle {
	private final Map<Expression, Signal<Value>> ilTrace;
	private final Signal<Value> macrostepEnded;
	private int step = 0;
	private List<String> errors = new ArrayList<String>();
	private boolean macroStepShouldBeEnding = false;
	private String lastEndingReason = "";
	private ReverseTranslationMap mapper;
	
	public LustreComplianceChecker(Map<Expression, Signal<Value>> ilTrace,
			Signal<Value> macrostepEnded,
			ReverseTranslationMap mapper) {
		this.ilTrace = ilTrace;
		this.macrostepEnded = macrostepEnded;
		this.mapper = mapper;
	}

	@Override
	public void endOfMicroStepBeforeCommit(JavaPlan plan) {
		// Check everything when the micro step ends, but just before
		// those changes actually become visible. This way, we will see
		// the initial state, and since inputs change just before
		// macro steps begin, we'll see the new input values correctly.
		ILSimulator sim = (ILSimulator) plan;
		System.out.println("Checking Lustre step "+step);
		
		checkMacroStepBoundary();
		checkAllExpressions(sim);
		exceptionIfErrorsExist();

		step++;
	}
	
	private void checkMacroStepBoundary() {
		boolean lustreMacroStepEnd = macrostepEnded.getValue(step)
				.toString().equalsIgnoreCase("true");
		if (macroStepShouldBeEnding) {
			if ( ! lustreMacroStepEnd) {
				// Uh oh. Lustre doesn't think that this macro step is ending.
				errors.add("Lustre didn't think the macro step was ending, "
						+ "but the IL ended because "+lastEndingReason);
			} else {
				// They both agree. Clear the flag and move on.
				macroStepShouldBeEnding = false;
				lastEndingReason = "";
			}
		} else if (lustreMacroStepEnd) {
			errors.add("Lustre thought the macro step was ending, but IL didn't.");
		}

	}
	
	private void checkAllExpressions(ILSimulator sim) {
		for (Expression expr : ilTrace.keySet()) {
			checkValue(expr, sim);
		}
	}
	
	private void exceptionIfErrorsExist() {
		if (errors.size() != 0) {
			errors.add("Macrostep info from Lustre: "+generateHistory(macrostepEnded));
			
			throw new RuntimeException("Error(s) at microstep "+step+": "
					+errors.stream().collect(Collectors.joining(", ")));
		} 
	}

	private void endOfMacroStep(String reason) {
		macroStepShouldBeEnding = true;
		lastEndingReason = reason;
	}

	private void checkValue(Expression e, ILSimulator sim) {
		PValue expected = sim.eval(e);
		Value actual = ilTrace.get(e).getValue(step);
		String expectedStr = RegressionTest.hackyILExprToLustre(expected, e.getType(), mapper);
		
		//TODO: is this really the best way to compare them?
		if ( expectedStr == null || actual == null || 
				! expectedStr.equals(actual.toString())) {
			String history = generateHistory(ilTrace.get(e));
			errors.add("Values for "+e+" don't match: expected "+expected+", "
					+"which should be "+expectedStr
					+" in Lustre, but instead saw "+actual
					+ " "+history);
		}
	}
	
	private String generateHistory(Signal<Value> v) {
		int start = Math.max(0, step-2);
		int end = step + 3;
		
		String history = "(history: ";
		for (int i = start; i < end; i++) {
			if (i == step) {
				history += "["+v.getValue(i)+"] ";
			} else {
				history += v.getValue(i)+" ";
			}
		}
		history += ")";
		return history;
	}

	@Override
	public void checkQuiescenceReached(JavaPlan plan) {
		endOfMacroStep("quiescence was reached");
	}

	@Override
	public void checkPrematureEndOfMacroStep(JavaPlan plan) {
		endOfMacroStep("something ended it prematurely");
	}
}