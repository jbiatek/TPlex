package edu.umn.crisys.plexil.test.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jkind.lustre.values.Value;
import jkind.results.Signal;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class LustreComplianceChecker extends TestOracle {
	private final Map<Expression, Signal<Value>> ilTrace;
	private final Signal<Value> macrostepEnded;
	private int step = 0;
	private List<String> errors = new ArrayList<String>();
	private boolean lustreSaysMacroStepEnded = false;

	public LustreComplianceChecker(Map<Expression, Signal<Value>> ilTrace,
			Signal<Value> macrostepEnded) {
		this.ilTrace = ilTrace;
		this.macrostepEnded = macrostepEnded;
	}

	@Override
	public void beforePlanExecution(JavaPlan plan) {
		// Check the initial state of everything
		checkThisStep(((ILSimulator) plan));
	}

	@Override
	public void checkEndOfMicroStepAfterCommit(JavaPlan plan) {
		// Check everything now that it has been committed. 
		checkThisStep(((ILSimulator) plan));
	}

	private void checkThisStep(ILSimulator sim) {
		System.out.println("Checking Lustre step "+step);
		if (lustreSaysMacroStepEnded) {
			// Oops, this didn't get cleared. We didn't see a macro step
			// ending, Lustre was wrong.
			errors.add("At step "+(step)+", Lustre said the macro step"
					+ " would end, but it didn't.");
		} else if (macrostepEnded.getValue(step)
				.toString().equalsIgnoreCase("true")) {
			// This step should be the last one in the macro step. The
			// macro step method should get called and it'll turn this 
			// back off. 
			lustreSaysMacroStepEnded = true;
		}

		
		for (Expression expr : ilTrace.keySet()) {
			checkValue(expr, sim);
		}
		if (errors.size() != 0) {
			throw new RuntimeException("Error(s) at microstep "+step+": "
					+errors.stream().collect(Collectors.joining(", ")));
		} else {
			step++;
		}
		
	}

	private void endOfMacroStep(String reason) {
		if ( ! lustreSaysMacroStepEnded) {
			// Ooh, Lustre didn't see this coming. That's not right.
			errors.add("At step "+step+", Lustre said the macro step"
					+ " wasn't over, but it was because "+reason);
		} else {
			lustreSaysMacroStepEnded = false;
			// For this entire step in Lustre nothing is supposed
			// to change except for inputs. We don't really care
			// what the values are until the next step. 
			step++;
		}
	}

	private void checkValue(Expression e, ILSimulator sim) {
		PValue expected = sim.eval(e);
		Value actual = ilTrace.get(e).getValue(step);
		String expectedStr = RegressionTest.hackyILExprToLustre(expected, e.getType());
		
		//TODO: is this really the best way to compare them?
		if ( expectedStr == null || actual == null || 
				! expectedStr.equals(actual.toString())) {
			//This is an error, give some info back
			String history = " (history: ";
			for (int i = Math.max(0, step-2); i < step+3; i++) {
				if (i == step) {
					history += "["+ilTrace.get(e).getValue(i)+"] ";
				} else {
					history += ilTrace.get(e).getValue(i)+" ";
				}
			}
			history += ")";
			
			errors.add("Values for "+e+" don't match: expected "+expected+", "
					+"which should be "+expectedStr
					+" in Lustre, but instead saw "+actual
					+ history);
		}
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