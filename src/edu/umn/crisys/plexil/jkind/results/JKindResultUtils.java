package edu.umn.crisys.plexil.jkind.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.ILExprToLustre;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.plx.QuiescenceLimitExceeded;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
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
		
		LustreSimulator lustreSim = new LustreSimulator(program);
		List<String> varsToGet = new ArrayList<>();
		// Use lustreSim to get these names, they might be inlined and 
		// therefore different from the Program
		varsToGet.addAll(lustreSim.getInputVars());
		varsToGet.addAll(lustreSim.getLocalVars());
		List<LustreTrace> rawResults = lustreSim.simulate(inputs, Simulation.PARTIAL, varsToGet);
		
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
							new BigFraction(new BigInteger("0"))));
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
	
	public static Optional<PlexilScript> translateToScript(String name, LustreTrace trace,
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
		try {
			sim.runPlanToCompletion();
		} catch (QuiescenceLimitExceeded q) {
			// This is a problem with their plan, not us. At least, it passed
			// compliance the whole time as far as we knew. 
			
			// As such, they'll probably want this test. Do nothing. 
			System.err.println("Quiescence timed out for this test, but I'm"
					+ " giving you the test case since it might be a problem"
					+ " with the plan");
		} catch (Exception e) {
			// Wrap with some more information
			System.err.println("Error translating "+name
					+" to PLEXILScript:");
			e.printStackTrace();
			return Optional.empty();
		}
		
		// No exceptions means that it's all good!
		PlexilScript theScript = recorder.convertToPlexilScript(name);
		return Optional.of(theScript);
		
	}
	private static String getType(Value v) {
		if (v == null) {
			throw new NullPointerException("Null value has no type");
		} else if (v instanceof jkind.lustre.values.BooleanValue) {
			return "bool";
		} else if (v instanceof jkind.lustre.values.IntegerValue) {
			return "int";
		} else if (v instanceof jkind.lustre.values.EnumValue) {
			return "enum";
		} else if (v instanceof jkind.lustre.values.RealValue) {
			return "real";
		} else {
			throw new RuntimeException("Don't know type of "+v.getClass());
		}
	}
	
	public static PValue parseValue(Value value, 
			ReverseTranslationMap stringMap) {
		switch(getType(value)) {
		case "int":
			return IntegerValue.get(Integer.parseInt(value.toString()));
		case "real":
			return RealValue.get(Double.parseDouble(value.toString()));
		case "bool":
			return BooleanValue.get(value.toString().equalsIgnoreCase("true"));
		case "enum":
			return LustreNamingConventions.reverseTranslate(value.toString(), 
					Optional.of(stringMap));
		default:
			throw new RuntimeException("Parsing for type "+getType(value)+" not implemented");
		}
	}


	public static Map<LustreTrace, Optional<PlexilScript>> translateToScripts(Map<String, LustreTrace> namedTraces,
			ReverseTranslationMap stringMapForLus, Plan ilPlan) {
		return namedTraces.entrySet().stream()
			.collect(Collectors.toMap(e -> e.getValue(), 
					e -> translateToScript(e.getKey(), e.getValue(), 
							stringMapForLus, ilPlan)));
	}
	
	public static Map<ILExpr, List<PValue>> createILMapFromLustre(
			LustreTrace fullTrace, Plan ilPlan, ReverseTranslationMap mapper) {
		// Put the trace in terms of variable names
		Map<String, Signal<Value>> stringTrace = new HashMap<>();
		fullTrace.getVariableNames().forEach(
				name -> stringTrace.put(name, fullTrace.getVariable(name)));
		
		// Now, for each IL expression we're interested in, find it in Lustre. 
		final Map<ILExpr, List<PValue>> ilTrace = new HashMap<>();
		// States aren't stored as variables, so those first
		ilPlan.getMachines().forEach(nsm -> 
				nsm.getNodeIds().forEach(uid -> 
				attachILExprToLustreVar(new GetNodeStateExpr(uid), 
						stringTrace, ilTrace, mapper)));
		// Then all variables from the plan
		try { 
			ilPlan.getVariables().forEach(var -> 
				attachILExprToLustreVar(var, stringTrace, ilTrace, mapper));
		} catch (Exception e) {
			System.out.println("\n\n\n\n\n"+fullTrace);
			throw e;
		}
		return ilTrace;
	}

	/**
	 * Take an IL expression and the map of Lustre IDs and values over time,
	 * and add the IL expression to the given map of IL expressions and their
	 * PValues over time. 
	 * 
	 * @param e
	 * @param stringTrace
	 * @param ilTrace
	 * @param mapper
	 */
	private static void attachILExprToLustreVar(ILExpr e, 
			Map<String, Signal<Value>> stringTrace,
			Map<ILExpr, List<PValue>> ilTrace,
			ReverseTranslationMap mapper) {
		if (e instanceof ILVariable
				&& LustreNamingConventions.hasValueAndKnownSplit((ILVariable) e)) {
			// Actually, we must split these into two
			if (e instanceof ArrayVar) {
				// Complicated... these two arrays are further split into 
				// indexes. 
				ArrayVar v = (ArrayVar) e;
				String valueArrayName = LustreNamingConventions.getNumericVariableValueId(v);
				String knownArrayName = LustreNamingConventions.getNumericVariableKnownId(v);
				for (int i = 0; i < v.getMaxSize(); i++) {
					String valIndex = valueArrayName+"["+i+"]";
					String knownIndex = knownArrayName+"["+i+"]";
					if (stringTrace.containsKey(valIndex)
							&& stringTrace.containsKey(knownIndex)) {
						// That's what we need!
						ilTrace.put(ILOperator.arrayIndex(v, IntegerValue.get(i)), 
								reverseTranslate(
										stringTrace.get(valIndex), 
										stringTrace.get(knownIndex), 
										v.getType().elementType()));
					} else {
						throw new RuntimeException("Looking for a numeric array index, " +
								valueArrayName +" or "+knownArrayName+" was not found in Lustre trace.");
					}
				}
			} else {
				// Find the two names and translate back
				ILVariable v = (ILVariable) e;
				String valueName = LustreNamingConventions.getNumericVariableValueId(v);
				String knownName = LustreNamingConventions.getNumericVariableKnownId(v);
				if (stringTrace.containsKey(valueName)
						&& stringTrace.containsKey(knownName)) {
					// Bingo
					ilTrace.put(e, reverseTranslate(
							stringTrace.get(valueName), 
							stringTrace.get(knownName), 
							v.getType()));
				} else {
					throw new RuntimeException("Looking for a numeric variable, " +
							valueName +" or "+knownName+" was not found in Lustre trace.");
				}
			}
		} else {
			// The rest are simpler, it's a 1:1 translation.

			String lustreString = hackyILExprToLustre(e, e.getType(), mapper);

			if (e instanceof ArrayVar) {
				// In the trace these are split up by index
				ArrayVar array = (ArrayVar) e;
				for (int i = 0; i < array.getMaxSize(); i++) {
					String indexed = lustreString+"["+i+"]";
					if (stringTrace.containsKey(indexed)) {
						ilTrace.put(ILOperator.arrayIndex(array, IntegerValue.get(i)), 
								reverseTranslate(
										stringTrace.get(indexed), 
										array.getType().elementType(), 
										mapper));
					} else {
						throw new RuntimeException("Couldn't find array index "+indexed
								+ " in Lustre trace" +
								"(the Lustre trace had "+stringTrace.size()+" entries)");
					}
				}
			} else if (stringTrace.containsKey(lustreString)) {
				ilTrace.put(e, reverseTranslate(
						stringTrace.get(lustreString),
						e.getType(),
						mapper));
			} else {
				throw new RuntimeException("Didn't find IL expression in Lustre trace: "
						+e+", was looking for it in Lustre as "+lustreString+
						"(the Lustre trace had "+stringTrace.size()+" entries)");
			}
		}
	}

	private static List<PValue> reverseTranslate(
			Signal<Value> valueSignal,
			Signal<Value> knownSignal,
			ILType type) {
		List<PValue> filled = new ArrayList<>();
		
		for (int i = 0; i < valueSignal.getValues().size(); i++) {
			if ( ! valueSignal.getValues().containsKey(i)) {
				throw new RuntimeException("Hole in Lustre signal "+valueSignal+
						": step "+i+" is not present!");
			}
			
			filled.add(LustreNamingConventions.reverseTranslateNumber(
							valueSignal.getValue(i).toString(), 
							knownSignal.getValue(i).toString(), 
							type));
		}
		
		return filled;
	}
	
	
	private static List<PValue> reverseTranslate(Signal<Value> signal, 
			ILType type,
			ReverseTranslationMap map) {
		if (LustreNamingConventions.hasValueAndKnownSplit(type)) {
			throw new RuntimeException("This needs to be split: "+type);
		}
		
		List<PValue> filled = new ArrayList<>();
		
		for (int i=0; i < signal.getValues().size(); i++) {
			// Check that there aren't any "holes" in the signal
			if ( ! signal.getValues().containsKey(i)) {
				throw new RuntimeException("Hole in Lustre signal "+signal+
						": step "+i+" is not present!");
			}
			filled.add(LustreNamingConventions.reverseTranslate(
					signal.getValue(i).toString(), 
					Optional.of(map)));
			
		}
		return filled;
	}

	public static String hackyILExprToLustre(ILExpr e, ILType type, ReverseTranslationMap mapper) {
		if (e instanceof ILVariable) {
			return LustreNamingConventions.getVariableId((ILVariable) e);
		} else if (e instanceof GetNodeStateExpr) {
			return LustreNamingConventions.getStateMapperId(
					((GetNodeStateExpr) e).getNodeUid());
		} else {
			ILExprToLustre il2lustre = new ILExprToLustre(mapper);
			return ILExprToLustre.exprToString(e.accept(il2lustre, type));
		}
		
	}


}
