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
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.test.java.LustreComplianceChecker;
import edu.umn.crisys.plexil.test.java.RegressionTest;
import enums.Simulation;

public class JKindResultUtils {

	private static final String COMMAND_HANDLE_SUFFIX = "__command_handle";
	
	public static jkind.lustre.Program parseProgram(File lusFile) 
			throws RecognitionException, IOException {
		return jkind.Main.parseLustre(lusFile.getPath());
	}
	
	
	public static LustreTrace concatenate(LustreTrace first, LustreTrace second) {
		LustreTrace newTrace = clone(first);
		newTrace.addLustreTrace(second);
		return newTrace;
	}
	
	public static LustreTrace clone(LustreTrace original) {
		LustreTrace newTrace = new LustreTrace(original.getLength());
		for (String variable : original.getVariableNames()) {
			Signal<Value> newSignal = new Signal<>(variable);
			for (Entry<Integer, Value> e : 
				original.getVariable(variable).getValues().entrySet()) {
				newSignal.putValue(e.getKey(), e.getValue());
			}
			newTrace.addVariable(newSignal);
		}
		return newTrace;
	}
	
	public static LustreTrace repeatLastStep(LustreTrace trace, int numTimes) {
		int originalLength = trace.getLength();
		int newLength = originalLength + numTimes;
		LustreTrace newTrace = new LustreTrace(newLength);
		for (String name : trace.getVariableNames()) {
			Signal<Value> oldVar = trace.getVariable(name);
			Signal<Value> newVar = new Signal<Value>(name);
			Value lastValue = oldVar.getValue(originalLength-1);
			for (int i = 0; i < newLength; i++) {
				if (i < originalLength) {
					newVar.putValue(i, oldVar.getValue(i));
				} else {
					newVar.putValue(i, lastValue);
				}
			}
			newTrace.addVariable(newVar);
		}
		return newTrace;
	}
	
	public static LustreTrace extendTestCase(LustreTrace inputs, Program program, int numSteps) {
		LustreTrace lengthenedInputs = repeatLastStep(inputs, numSteps);
		return simulate(program, lengthenedInputs);
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
		List<LustreTrace> rawResults = lustreSim.simulate(inputs, Simulation.COMPLETE, varsToGet);
		
		// Enums and such are all integers in this trace. Let's fix that.
		
		List<LustreTrace> newResults = rawResults.stream()
				.map(raw -> reEnumerate(raw, typeTable))
				.collect(Collectors.toList());
		
		return newResults;
	}
	
	public static LustreTrace unEnumerate(LustreTrace t, Program p) {
		return unEnumerate(t, ResolvedTypeTable.get(p));
	}
	
	public static LustreTrace unEnumerate(LustreTrace t, Map<String,Type> typeTable) {
		LustreTrace newTrace = new LustreTrace(t.getLength());
		for (String varName : t.getVariableNames()) {
			Signal<Value> oldVar = t.getVariable(varName);
			Type type = typeTable.get(varName);
			
			newTrace.addVariable(unEnumerateSignal(oldVar, type));
		}
		return newTrace;
	}
	
	public static Signal<Value> unEnumerateSignal(Signal<Value> oldVar, Type type) {
		if (type instanceof EnumType) {
			// We need to ensure that this value gets turned into an int
			if (oldVar.getValue(0) instanceof EnumValue) {
				// Here we go, let's change this
				Signal<Value> newVar = new Signal<Value>(oldVar.getName());
				oldVar.getValues().keySet().forEach(step -> {
					// Get old value as enum
					EnumValue oldValue = (EnumValue) oldVar.getValue(step);
					newVar.putValue(step, values.StringToValue.get(oldValue.value, type));
				});
				return newVar;
			} else if (oldVar.getValue(0) instanceof jkind.lustre.values.IntegerValue) {
				// Our work is already done
				return oldVar;
			} else {
				throw new RuntimeException("Passed in type was "+type
						+" but actual values were "+oldVar.getValue(0).getClass());
			}
		} else {
			// No need to change this
			return oldVar;
		}
	}
	
	public static LustreTrace reEnumerate(LustreTrace t, Program p) {
		return reEnumerate(t, ResolvedTypeTable.get(p));
	}
	
	public static LustreTrace reEnumerate(LustreTrace t, Map<String,Type> typeTable) {
		LustreTrace newTrace = new LustreTrace(t.getLength());
		for (String varName : t.getVariableNames()) {
			Signal<Value> oldVar = t.getVariable(varName);
			Type type = typeTable.get(varName);
			
			newTrace.addVariable(reEnumerateSignal(oldVar, type));
		}
		return newTrace;
	}
	

	public static Signal<Value> reEnumerateSignal(Signal<Value> oldVar, Type type) {
		if (type instanceof EnumType) {
			EnumType enumType = (EnumType) type;
			Signal<Value> newVar = new Signal<Value>(oldVar.getName());
			oldVar.getValues().keySet().forEach(step -> {
				if (oldVar.getValue(step) == null) {
					throw new NullPointerException(oldVar.getName()+" null at step "+step);
				}
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
		return simulate(lustreProgram, unEnumerate(inputTrace, lustreProgram));
	}
	
	public static Map<String, Counterexample> extractCounterexamples(JKindResult results) {
		return results.getPropertyResults().stream()
			.map(propResult -> propResult.getProperty())
			.filter(prop -> prop instanceof InvalidProperty)
			.map(prop -> (InvalidProperty) prop)
			.collect(Collectors.toMap(Property::getName, 
					InvalidProperty::getCounterexample));
	}
	
	public static Map<String, LustreTrace> extractTraces(JKindResult result, Program p) {
		return extractCounterexamples(result).entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> toTrace(e.getValue(), p)));
	}
	
	public static Counterexample toCounterexample(LustreTrace trace) {
		Counterexample cex = new Counterexample(trace.getLength());
		for (String varName : trace.getVariableNames()) {
			cex.addSignal(trace.getVariable(varName));
		}
		return cex;
	}
	
	public static PlexilScript translateToScript(String name, LustreTrace trace,
			ReverseTranslationMap map, Plan ilPlan) {
		
		JavaPlan.DEBUG = true;
		ScriptedEnvironment.DEBUG = true;

		// Use the recorder as the environment, it will create proper events
		// from the Lustre trace that we give it. 
		ScriptRecorderFromLustreData recorder = new ScriptRecorderFromLustreData(trace, map);
		LustreComplianceChecker checker = 
				RegressionTest.createComplianceChecker(trace, ilPlan, map);
		ILSimulator sim = new ILSimulator(ilPlan, recorder);
		sim.addObserver(recorder);
		sim.addObserver(checker);
		sim.runPlanToCompletion();
		
		// No exceptions means that it's all good!
		PlexilScript theScript = recorder.convertToPlexilScript(name);
		return theScript;
		
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
	
	public static PValue parseValue(Value value, 
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


	public static Map<LustreTrace, PlexilScript> translateToScripts(Map<String, LustreTrace> namedTraces,
			ReverseTranslationMap stringMapForLus, Plan ilPlan) {
		return namedTraces.entrySet().stream()
			.collect(Collectors.toMap(e -> e.getValue(), 
					e -> translateToScript(e.getKey(), e.getValue(), 
							stringMapForLus, ilPlan)));
	}


}
