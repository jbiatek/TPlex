package edu.umn.crisys.plexil.jkind.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import utils.LustreException;
import jkind.api.Backend;
import jkind.api.results.JKindResult;
import jkind.api.xml.XmlParseThread;
import jkind.lustre.Type;
import jkind.lustre.values.Value;
import jkind.results.Counterexample;
import jkind.results.InvalidProperty;
import jkind.results.Property;
import jkind.results.Signal;
import lustre.LustreProgram;
import lustre.LustreTrace;
import lustre.LustreVariable;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;

public class JKindResultUtils {

	private static final String COMMAND_HANDLE_SUFFIX = "__command_handle";
	
	public static JKindResult parseJKindFile(File xml) throws IOException {
		String baseName = xml.getName().replaceFirst("\\.xml$", "");
		return parseJKindFile(new FileInputStream(xml), baseName);
	}
	
	public static JKindResult parseJKindFile(InputStream in, String name) {
		JKindResult result = new JKindResult(name);
		XmlParseThread parser = new XmlParseThread(in, result, Backend.JKIND);
		// We can just run this on this thread.
		parser.run();
		return result;
	}
	
	public static List<PlexilScript> translateToScripts(JKindResult results,
			ReverseTranslationMap reverseMap) {
		return extractCounterexamples(results).entrySet().stream()
				.map(entry -> translateToScript(
						entry.getKey(), entry.getValue(), reverseMap))
				.collect(Collectors.toList());
	}
	
	public static List<LustreTrace> translateToTraces(JKindResult results,
			LustreProgram prog) {
		return extractCounterexamples(results).entrySet().stream()
				.map(entry -> toTrace(entry.getValue(), prog))
				.collect(Collectors.toList());
	}
	
	//TODO: Replace this with Dongjiang's code when it's public
	private static LustreTrace toTrace(Counterexample ce, LustreProgram lustreProgram) {
		LustreTrace output = new LustreTrace(ce.getLength());
		List<String> allVars = new ArrayList<String>();
		allVars.addAll(lustreProgram.getMainNode().getInputVars());
		allVars.addAll(lustreProgram.getMainNode().getOutputVars());
		allVars.addAll(lustreProgram.getMainNode().getLocalVars());
		
		for (String var : allVars) {
			try {
				Type type = lustreProgram.getMainNode().getVarType(var);

				LustreVariable lv = new LustreVariable(var, type);

				int length = ce.getLength();

				Signal<Value> signal = ce.getSignal(var);

				for (int step = 0; step < length; step++) {
					// If JKind does not produce values for this variable
					// or does not produce value at a step
					// Add a null value
					if (signal == null || signal.getValue(step) == null) {
						lv.putValue(step, null);
					} else {
						lv.putValue(step, signal.getValue(step));
					}
				}
				output.addVariable(lv);
			} catch (LustreException e) {
				throw new RuntimeException(e);
			}
		}

		return output;
	}

	public static Map<String, Counterexample> extractCounterexamples(JKindResult results) {
		return results.getPropertyResults().stream()
			.map(propResult -> propResult.getProperty())
			.filter(prop -> prop instanceof InvalidProperty)
			.map(prop -> (InvalidProperty) prop)
			.collect(Collectors.toMap(Property::getName, 
					InvalidProperty::getCounterexample));
	}

	public static PlexilScript translateToScript(String name, Counterexample cex,
			ReverseTranslationMap map) {
		PlexilScript script = new PlexilScript(name);
		
		List<Signal<Value>> relevant = cex.getSignals().stream()
				.filter(sig -> 
					// We want raw lookup inputs
					(sig.getName().startsWith("Lookup") && sig.getName().endsWith("__raw"))
					// We also want command handle inputs
					|| (sig.getName().endsWith(COMMAND_HANDLE_SUFFIX))
					)
				.collect(Collectors.toList());
		int[] macroStepBoundaries = 
				cex.getBooleanSignal(LustreNamingConventions.MACRO_STEP_ENDED_ID)
				.getValues().entrySet().stream()
				// What steps is the macro step over? (Also, we want the first
				// step regardless)
				.filter(e -> e.getValue().value || e.getKey() == 0)
				// Get those step numbers, but we are actually interested in the
				// interim step, which happens 1 step later, unless it's the
				// initial step which we do want as-is.
				.mapToInt(e -> e.getKey() == 0 ? 0 : e.getKey() + 1)
				// In order, please
				.sorted()
				// And save it
				.toArray();
		
		
		
		for (int step : macroStepBoundaries) {
			if (step == cex.getLength()) {
				// Oop, this end of macrostep doesn't have an interim because
				// it's the end. 
				continue;
			}
			Simultaneous thisMacroStep = new Simultaneous();
			
			for (Signal<Value> signal : relevant) {
				// Read this value as a PValue
				PValue plexilValue = parseValue(signal.getValue(step), map);
				// Based on the signal name, create a PlexilScript event
				toScriptEvent(signal.getName(), plexilValue, map)
					.ifPresent(e -> thisMacroStep.addEvent(e));
			}
			if (step == 0 ) {
				script.addInitialEvent(thisMacroStep.getCleanestEvent());
			} else {
				script.addMainEvent(thisMacroStep.getCleanestEvent());
			}
		}
		return script;
	}
	
	private static String getType(Value v) {
		if (v instanceof jkind.lustre.values.BooleanValue) {
			return "bool";
		} else if (v instanceof jkind.lustre.values.IntegerValue) {
			return "int";
		} else if (v instanceof jkind.lustre.values.EnumValue) {
			return "enum";
		} else {
			throw new RuntimeException("Don't know type of "+v);
		}
	}
	
	private static Optional<Event> toScriptEvent(String signal, PValue v, 
			ReverseTranslationMap map) {
		if (signal.startsWith("Lookup__")) {
			String stateName = signal.replaceFirst("Lookup__", "");
			return Optional.of(new StateChange(new FunctionCall(stateName), v));
		} else if (signal.endsWith(COMMAND_HANDLE_SUFFIX)) {
			// If this is unknown, no point in creating an event. But if it 
			// is, we'll need the command's name from the map.
			if (v.isKnown()) {
				FunctionCall call = new FunctionCall(
						map.getCommandNameFromHandleId(signal).get());
				return Optional.of(new CommandAck(call, (CommandHandleState)v));
			} else {
				// No point in acknowlidging with nothing
				return Optional.empty();
			}
		}
		throw new RuntimeException("Signal "+signal
				+" doesn't have a translation to PlexilScript");
	}

	private static PValue parseValue(Value value, 
			ReverseTranslationMap stringMap) {
		switch(getType(value)) {
		case "int":
			return IntegerValue.get(Integer.parseInt(value.toString()));
		case "bool":
			return ExprType.BOOLEAN.parseValue(value.toString());
		case "enum":
			return LustreNamingConventions.reverseTranslate(value.toString(), 
					Optional.of(stringMap));
		default:
			throw new RuntimeException("Parsing for type "+getType(value)+" not implemented");
		}
	}


}
