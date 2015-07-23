package edu.umn.crisys.plexil.jkind.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RecognitionException;

import jkind.api.Backend;
import jkind.api.results.JKindResult;
import jkind.api.xml.XmlParseThread;
import jkind.lustre.ArrayType;
import jkind.lustre.EnumType;
import jkind.lustre.NamedType;
import jkind.lustre.Program;
import jkind.lustre.RecordType;
import jkind.lustre.SubrangeIntType;
import jkind.lustre.TupleType;
import jkind.lustre.Type;
import jkind.lustre.VarDecl;
import jkind.lustre.values.EnumValue;
import jkind.lustre.values.Value;
import jkind.lustre.visitors.TypeVisitor;
import jkind.results.Counterexample;
import jkind.results.InvalidProperty;
import jkind.results.Property;
import jkind.results.Signal;
import jkind.util.BigFraction;
import lustre.LustreTrace;
import simulation.LustreSimulator;
import types.ResolvedTypeTable;
import values.ValueToString;
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
import enums.Simulation;

public class JKindResultUtils {

	private static final String COMMAND_HANDLE_SUFFIX = "__command_handle";
	
	public static jkind.lustre.Program parseProgram(File lusFile) 
			throws RecognitionException, IOException {
		return jkind.Main.parseLustre(lusFile.getPath());
	}
	
	public static List<LustreTrace> simulateCSV(File lustreFile, String mainNode, 
			File inputCsv) throws Exception{

		Program program = parseProgram(lustreFile);
		
		
		
		List<LustreTrace> inputs = testsuite.ReadTestSuite
				.read(inputCsv.getPath(), program);
		
		return simulate(program, inputs);
	}
	
	public static LustreTrace simulate(Program program, LustreTrace input) {
		return simulate(program, Arrays.asList(input)).get(0);
	}
	
	public static List<LustreTrace> simulate(Program program, List<LustreTrace> inputs) {
		// Check traces for incompleteness first
		Map<String, Type> typeTable = ResolvedTypeTable.get(program);
		
		for (LustreTrace input : inputs) {
			for (VarDecl varDecl : program.getMainNode().inputs) {
				String name = varDecl.id;
				Type type = typeTable.get(name);
				if (input.getVariable(name) == null) {
					System.err.println("Warning: Variable "+varDecl+" is null, filling with defaults");
					Signal<Value> newVar = new Signal<Value>(varDecl.id);
					for (int i=0; i < input.getLength(); i++) {
						newVar.putValue(i, getDefaultValueFor(type).get());
					}
					input.addVariable(newVar);
				} else {
					Signal<Value> var = input.getVariable(name);
					for (int i = 0; i < input.getLength(); i++) {
						if (var.getValue(i) == null) {
							System.err.println("Warning: Variable "+varDecl+
									" has a null value for step "+i+", filling with default");
							var.putValue(i, getDefaultValueFor(type).get());
						}
					}
				}
			}
		}
		
		
		LustreSimulator lustreSim = new LustreSimulator(program);
		List<String> varsToGet = new ArrayList<>();
		// Use lustreSim to get these names, they might be inlined and 
		// therefore different from the Program
		varsToGet.addAll(lustreSim.getInputVars());
		varsToGet.addAll(lustreSim.getLocalVars());
		List<LustreTrace> rawResults = lustreSim.simulate(inputs, Simulation.PARTIAL, varsToGet);
		
		// Enums and such are all integers in this trace. Let's fix that.
		
		List<LustreTrace> newResults = new ArrayList<>();
		
		for (LustreTrace oldTrace : rawResults) {
			LustreTrace newTrace = new LustreTrace(oldTrace.getLength());
			for (String varName : oldTrace.getVariableNames()) {
				Signal<Value> oldVar = oldTrace.getVariable(varName);
				Type type = typeTable.get(varName);
				
				newTrace.addVariable(reEnumerateSignal(oldVar, type));
			}
			newResults.add(newTrace);
		}
		
		return newResults;
	}
	
	public static Signal<Value> reEnumerateSignal(Signal<Value> oldVar, Type type) {
		if (type instanceof EnumType) {
			EnumType enumType = (EnumType) type;
			Signal<Value> newVar = new Signal<Value>(oldVar.getName());
			oldVar.getValues().keySet().forEach(step -> {
				newVar.putValue(step, new EnumValue(ValueToString.get(
						oldVar.getValue(step), enumType)));
			});
			return newVar;
		} else {
			// No need to change this
			return oldVar;
		}

	}
	
	public static JKindResult parseJKindFile(File xml) throws IOException {
		String baseName = xml.getName().replaceFirst("\\.xml$", "");
		return parseJKindFile(new FileInputStream(xml), baseName);
	}
	
	public static JKindResult parseJKindFile(InputStream in, String name) {
		JKindResult result = new JKindResult(name);
		XmlParseThread parser = new XmlParseThread(in, result, Backend.JKIND);
		// We can just run this on this thread.
		parser.run();
		if (parser.getThrowable() != null) {
			throw new RuntimeException(parser.getThrowable());
		}
		
		
		return result;
	}
	
	public static Map<Counterexample,PlexilScript> translateToScripts(JKindResult results,
			ReverseTranslationMap reverseMap) {
		return namedCounterexamplesToScripts(extractCounterexamples(results), reverseMap);
	}
	
	public static Map<Counterexample,PlexilScript> translateToScripts(
			Map<String,LustreTrace> namedTraces, ReverseTranslationMap reverseMap) {
		Map<String, Counterexample> namedCexes = namedTraces.entrySet().stream()
					.collect(Collectors.toMap(
							Entry::getKey, 
							e -> toCounterexample(e.getValue())));
		
		return namedCounterexamplesToScripts(namedCexes, reverseMap);
	}
	
	private static Map<Counterexample, PlexilScript> namedCounterexamplesToScripts(
			Map<String, Counterexample> cexes, ReverseTranslationMap reverseMap) {
		return cexes.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getValue, 
						entry -> translateToScript(
								entry.getKey(), entry.getValue(), reverseMap)
						));
	}
	
	public static List<LustreTrace> translateToTraces(JKindResult results,
			Program prog) {
		return extractCounterexamples(results).entrySet().stream()
				.map(entry -> toTrace(entry.getValue(), prog))
				.collect(Collectors.toList());
	}
	
	public static Optional<Value> getDefaultValueFor(Type type) {
		return type.accept(new TypeVisitor<Optional<Value>>() {

			private Optional<Value> noIdea(Type t) {
				return Optional.empty();
			}
			
			@Override
			public Optional<Value> visit(ArrayType e) {
				return noIdea(e);
			}

			@Override
			public Optional<Value> visit(EnumType e) {
				return Optional.of(new EnumValue(e.values.get(0)));
			}

			@Override
			public Optional<Value> visit(NamedType e) {
				if (type.equals(NamedType.BOOL)) {
					return Optional.of(jkind.lustre.values.BooleanValue.FALSE);
				} else if (type.equals(NamedType.INT)) {
					return Optional.of(new jkind.lustre.values.IntegerValue(
							new BigInteger("0")));
				} else if (type.equals(NamedType.REAL)) {
					return Optional.of(new jkind.lustre.values.RealValue(
							new BigFraction(new BigDecimal("0.0"))));
				} else {
					throw new IllegalArgumentException("Unknown type: " + type + " "
							+ type.getClass());
				}
			}

			@Override
			public Optional<Value> visit(RecordType e) {
				return noIdea(e);
			}

			@Override
			public Optional<Value> visit(TupleType e) {
				return noIdea(e);
			}

			@Override
			public Optional<Value> visit(SubrangeIntType e) {
				return Optional.of(new jkind.lustre.values.IntegerValue(e.low));
			}
			
		});
	}
	
	public static LustreTrace toTrace(Counterexample ce, Program lustreProgram) {
		LustreTrace inputTrace = new LustreTrace(ce.getLength());
		for (Signal<Value> var : ce.getSignals()) {
			inputTrace.addVariable(var);
		}
		// JKind's counterexamples inexplicably include some variables but not
		// others. We'll simulate instead, since we know what we'll get back
		// from that.
		return simulate(lustreProgram, inputTrace);
	}
	
	public static Map<String, Counterexample> extractCounterexamples(JKindResult results) {
		return results.getPropertyResults().stream()
			.map(propResult -> propResult.getProperty())
			.filter(prop -> prop instanceof InvalidProperty)
			.map(prop -> (InvalidProperty) prop)
			.collect(Collectors.toMap(Property::getName, 
					InvalidProperty::getCounterexample));
	}
	
	public static Counterexample toCounterexample(LustreTrace trace) {
		Counterexample cex = new Counterexample(trace.getLength());
		for (String varName : trace.getVariableNames()) {
			cex.addSignal(trace.getVariable(varName));
		}
		return cex;
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
