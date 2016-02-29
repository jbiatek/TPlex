package edu.umn.crisys.plexil.test.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jkind.lustre.values.Value;
import jkind.results.Signal;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.jkind.results.JKindResultUtils;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class LustreComplianceChecker extends TestOracle {
	private final Map<ILExpr, List<PValue>> ilTrace;
	private final Signal<Value> macrostepEnded;
	private int step = 0;
	private List<String> errors = new ArrayList<String>();
	private boolean macroStepShouldBeEnding = false;
	private String lastEndingReason = "";
	private ReverseTranslationMap mapper;
	
	public LustreComplianceChecker(Map<ILExpr, List<PValue>> ilTrace,
			Signal<Value> macrostepEnded,
			ReverseTranslationMap mapper) {
		this.ilTrace = ilTrace;
		this.macrostepEnded = macrostepEnded;
		if (macrostepEnded == null) {
			throw new RuntimeException("No macrostep data given");
		}
		this.mapper = mapper;
	}

	@Override
	public void beforeMicroStepRuns(JavaPlan plan) {
		// Check everything just before the next micro step begins. 
		// This way, we will see things the same way that Lustre does. 
		// In the Lustre translation, inputs change during the last micro step
		// of the macro step. (Since everything always uses the pre() value,
		// it's fine because the change isn't visible until the next step,
		// which is the beginning of a new macro step.) 
		
		// By always doing our check just before the micro step runs, we see
		// the world the same way: we won't try to read the last step of the
		// macro step until the next one is already about to start, meaning
		// that inputs have already been changed and those changes are visible.
		ILSimulator sim = (ILSimulator) plan;
		System.out.println("Checking Lustre step "+step);
		
		checkMacroStepBoundary();
		checkAllExpressions(sim);
		exceptionIfErrorsExist();

		step++;
	}
	
	private void checkMacroStepBoundary() {
		if (step >= macrostepEnded.getValues().size()) {
			return;
		}
		
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
		for (ILExpr expr : ilTrace.keySet()) {
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

	private void checkValue(ILExpr e, ILSimulator sim) {
		if (step >= ilTrace.get(e).size()) {
			return;
		}
		
		PValue expected = sim.eval(e);
		PValue actual = ilTrace.get(e).get(step);
		String expectedStr = JKindResultUtils.hackyILExprToLustre(expected, e.getType(), mapper);
		
		if ( expected == null || actual == null || 
				! expected.equals(actual)) {
			String history = generateHistory(ilTrace.get(e));
			errors.add("Values for "+e+" don't match: the simulator had "
					+expected+", "
					+"which should be "+expectedStr
					+" in Lustre, but the Lustre trace had "+actual
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

	
	private String generateHistory(List<PValue> v) {
		int start = Math.max(0, step-2);
		int end = step + 3;
		
		String history = "(history: ";
		for (int i = start; i < end; i++) {
			if (i == step) {
				history += "["+v.get(i)+"] ";
			} else {
				history += v.get(i)+" ";
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