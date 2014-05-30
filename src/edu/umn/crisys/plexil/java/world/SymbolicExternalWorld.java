package edu.umn.crisys.plexil.java.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.psx.FunctionCall;
import edu.umn.crisys.plexil.java.values.*;
import edu.umn.crisys.util.Pair;

/**
 * Returns symbolic values to a plan and sends PLEXILScript files to a listener
 * in JPF. Current limitations include:
 * 
 * <ul>
 * <li> Lookup arguments are ignored, so Lookup("temp", "sensor1") is the same
 * as Lookup("temp", "anything else").</li>
 * <li> Commands are only responded to once, with a return value if we've been
 * told that it should return something.</li>
 * <li> I don't know how to specify UNKNOWN values in PSX, so they aren't
 * ever created or returned. 
 * </ul>
 * @author jbiatek
 *
 */
public class SymbolicExternalWorld implements ExternalWorld {
	
	private final ValueSource symbolic;
	
	public SymbolicExternalWorld(ValueSource symbolicSource) {
		this.symbolic = symbolicSource;
	}
	
	
	
	private interface ValueGenerator<T extends PValue> {
		public T generateNewValue();
	}
	
	private class AnythingOfType<T extends PValue> implements ValueGenerator<T> {
		private PlexilType type;
		
		public AnythingOfType(PlexilType type) {
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T generateNewValue() {
			return (T) getSymbolicPValueOfType(type);
		}
		
	}
	
	private class AnyOfThese<T extends PValue> implements ValueGenerator<T> {
		private T[] values;
		
		public AnyOfThese(T... values) {
			this.values = values;
		}

		@Override
		public T generateNewValue() {
			if (values.length == 1) {
				return values[0];
			}
			
			return values[nondetIntIndex(values.length)];
		}
		
	}
	
	private class IncreasingNumber<T extends PNumeric> implements ValueGenerator<T> {
		private PlexilType type;
		private T lastValue;
		
		public IncreasingNumber(PlexilType type, T init) {
			if ( ! type.isNumeric()) {
				throw new RuntimeException("Cannot create non-numeric increasing lookup");
			}
			this.type = type; this.lastValue = init;
		}
		
		public IncreasingNumber(PlexilType type) {
			this(type, null);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T generateNewValue() {
			PNumeric newVal = (PNumeric) getSymbolicPValueOfType(type);
//			lastValue = (T) lastValue.max(newVal);
//			return lastValue;
			// Abandon this line of execution if the new value is less than 
			// the old one. 
			symbolic.continueOnlyIf(lastValue != null && newVal.ge(lastValue).isTrue());
			lastValue = (T) newVal.castTo(type);
			return lastValue;
		}
	}
	
	

	/*
	 * Symbolic value generators.
	 */
	
	
	private Map<String,ValueGenerator<?>> lookups = new HashMap<String, ValueGenerator<?>>();

	private Map<String,ValueGenerator<CommandHandleState>> cmdResults = 
			new HashMap<String, ValueGenerator<CommandHandleState>>();
	/**
	 * Values for commands to return, in addition to the command handle. 
	 */
	private Map<String,ValueGenerator<?>> cmdReturnValues = new HashMap<String, ValueGenerator<?>>();

	
	/*
	 * Storage for things that happen during a PLEXIL step.
	 */
	
	
	private List<UpdateHandler> updateQueue = new ArrayList<UpdateHandler>();
	private List<Pair<CommandHandler,FunctionCall>> cmdQueue = 
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
	private Map<String,PValue> oldLookupValues = new HashMap<String,PValue>();
	private Map<String,PValue> currentLookupValues =
			new HashMap<String, PValue>();
	
	
	
	private int currentStep = 0;
	private boolean somethingHappened = false;

	
	private PValue getSymbolicPValueOfType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return BooleanValue.get(symbolic.symbolicBoolean(true));
		case INTEGER:
			return IntegerValue.get(symbolic.symbolicInteger(0));
		case REAL:
		case NUMERIC:
			return RealValue.get(symbolic.symbolicDouble(0.0));
		default:
			throw new RuntimeException("Type "+t+
					" does not have a symbolic method");
		}

	}
	
	private int nondetIntIndex(int maximum) {
		int symbolicInt = symbolic.symbolicInteger(0);
		symbolic.continueOnlyIf(symbolicInt >= 0 && symbolicInt < maximum);
		return symbolicInt;
	}
	
	/**
	 * Create a Lookup which will return a symbolic value of the given
	 * type. 
	 * 
	 * @param lookup
	 * @param type
	 */
	public void addLookup(String lookup, PlexilType type) {
		lookups.put(lookup, new AnythingOfType<PValue>(type));
	}
	
	public void setLookupInitialValue(String lookup, PValue init) {
		currentLookupValues.put(lookup, init);
		// Output it now, since we already know it and it won't get output again
		lookupValueToXML(lookup, init);

	}
	
	public void addLookup(String lookup, PlexilType type, PValue initialValue) {
		addLookup(lookup, type);
		setLookupInitialValue(lookup, initialValue);
	}
	
	/**
	 * Create a Lookup which will return one of the given values symbolically.
	 * @param lookup
	 * @param values
	 */
	public void addLookup(String lookup, PValue... values) {
		lookups.put(lookup, new AnyOfThese<PValue>(values));
	}
	
	public void addLookup(PValue initialValue, String lookup, PValue... values) {
		addLookup(lookup, values);
		setLookupInitialValue(lookup, initialValue);
	}
	
	
	public void addIncreasingLookup(String lookup, PlexilType type) {
		lookups.put(lookup, new IncreasingNumber<PNumeric>(type));
	}
	
	public void addIncreasingLookup(String lookup, double initialValue) {
		lookups.put(lookup, new IncreasingNumber<PReal>(PlexilType.REAL, RealValue.get(initialValue)));
		setLookupInitialValue(lookup, RealValue.get(initialValue));
	}
	
	public void addIncreasingLookup(String lookup, int initialValue) {
		lookups.put(lookup, new IncreasingNumber<PInteger>(PlexilType.INTEGER, IntegerValue.get(initialValue)));
		setLookupInitialValue(lookup, IntegerValue.get(initialValue));
	}
	
	/**
	 * Create a Command which can respond with one of the given command handles.
	 * If none are given, any command handle is valid. 
	 * @param name
	 * @param states
	 */
	public void addCommand(String name, CommandHandleState... states) {
		cmdResults.put(name, new AnyOfThese<CommandHandleState>(states));
	}
	
	/**
	 * Create a Command which returns a symbolic value of the given type.
	 * @param name
	 * @param type
	 */
	public void addCommandReturn(String name, PlexilType type) {
		cmdReturnValues.put(name, new AnythingOfType<PValue>(type));
	}
	
	/**
	 * Create a Command which returns one of the given values. 
	 * @param name
	 * @param values
	 */
	public void addCommandReturn(String name, PValue...values) {
		cmdReturnValues.put(name, new AnyOfThese<PValue>(values));
	}
	
	private void respondToUpdate(UpdateHandler u) {
		u.acknowledgeUpdate();
		psxUpdateAck(u.getNodeName());
	}
	
	private void respondToCommand(CommandHandler handler, FunctionCall call) {
		// Does this command return a value? 
		// "There is an important aspect of scripting command handles when the 
		// command also returns a value. Namely, the handle must occur after 
		// the value."
		if (cmdReturnValues.containsKey(call.getName())) {
			returnValueToCommand(handler, call, 
					cmdReturnValues.get(call.getName()).generateNewValue());
		}
		// Now for the CommandAck. Every command should have this.
		commandAck(handler, call, cmdResults.get(call.getName()).generateNewValue());
	}
	
	private void returnValueToCommand(CommandHandler handler, FunctionCall call, PValue v) {
		handler.commandReturns(v);
		constructCommandXML(call, v, "");
	}
	
	private void commandAck(CommandHandler handler, FunctionCall call, CommandHandleState status) {
		handler.setCommandHandle(status);
		constructCommandXML(call, status, "Ack");
	}
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		afterMacroStepEnds(true);
	}
	
	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		afterMacroStepEnds(false);
	}
	
	
	@Override
	public void endOfMicroStep(JavaPlan plan) {
		// Do nothing until the macro step ends.
	}

	private void afterMacroStepEnds(boolean refreshLookups) {
		somethingHappened = false;
		
		// Did anything happen during the last step? If not, go back and 
		// try again.
		//Verify.ignoreIf(currentLookupValues.size() == 0 && updateQueue.size() == 0 && cmdQueue.size() == 0);
		
		if (currentStep == 0) {
			// We just completed the first step. That means that we're done with
			// the "initial state" values.
			psxInitialStateEnd();
		} else {
			// This isn't the first step, so it's actually a "Simultaneous tag that
			// needs to close.
			psxSimultaneousEnd();
		}
		// Now it's a new step.
		currentStep++;
		
		// Fresh new set of lookup values, but keep the old ones so that
		// we can make sure that when they change, they actually change.
		if (refreshLookups) {
			for (String newValue : currentLookupValues.keySet()) {
				oldLookupValues.put(newValue, currentLookupValues.get(newValue));
			}
			currentLookupValues.clear();
		}

		// Start a new Simultaneous tag for the next step.
		psxSimultaneousStart();
		
		
		for (UpdateHandler handler : updateQueue) {
			respondToUpdate(handler);
		}
		updateQueue.clear();

		Iterator<Pair<CommandHandler, FunctionCall>> iter = cmdQueue.iterator();
		while (iter.hasNext()) {
			Pair<CommandHandler, FunctionCall> pair = iter.next();
			//if (Verify.getBoolean()) {
				respondToCommand(pair.first, pair.second);
				iter.remove();
			//}
		}
		

		
		
		// We leave the Simultaneous tag open, so that when we generate new
		// Lookup values during the next PLEXIL step, it appears that they
		// changed here too. 
	}

	@Override
	public boolean stop() {
		// SPF will backtrack when depth is reached. 
		return false;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		updateQueue.add(node);
		somethingHappened = true;
	}

	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		return lookup(stateName);
	}

	@Override
	public PValue lookupOnChange(PString stateName, PNumeric tolerance,
			PValue... args) {
		return lookup(stateName);
	}
	
	private PValue lookup(PString stateName) {
		if (stateName.isUnknown()) {
			throw new RuntimeException("Lookup(UNKNOWN)");
		}
		String lookup = stateName.getString();
		if ( ! currentLookupValues.containsKey(lookup)) {
			if (oldLookupValues.containsKey(lookup) && symbolic.symbolicBoolean(true)) {
				// Value doesn't change
				return oldLookupValues.get(lookup);
			}
			
			
			PValue newVal = lookups.get(lookup).generateNewValue();
			// A new value that is actually exactly the same is pointless.
//			Verify.ignoreIf(oldLookupValues != null && 
//					oldLookupValues.containsKey(lookup) && 
//					oldLookupValues.get(lookup).equalTo(newVal).isTrue());
			
			
			currentLookupValues.put(lookup, newVal);
			lookupValueToXML(lookup, newVal);
			somethingHappened = true;
		}
		return currentLookupValues.get(lookup);
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		if (name.isUnknown()) {
			throw new RuntimeException("Called UNKNOWN function.");
		}
		cmdQueue.add(new Pair<CommandHandler, FunctionCall>(caller, 
				new FunctionCall(name.getString(), args)));
		somethingHappened = true;
	}

	private void constructCommandXML(FunctionCall call, PValue result, String action) {
		String type;
		if (result.getType() == PlexilType.COMMAND_HANDLE) {
			// PLEXILScript treats command handles as strings. 
			type = "string";
		} else {
			type = toPsxTypeString(result.getType());
		}
		
		psxCommand(action, call.getName(), type);
		for (PValue arg : call.getArgs()) {
			psxParam(toPsxTypeString(arg.getType()), unwrapValue(arg));
		}
		psxResultOrValue("Result", unwrapValue(result));
		
		psxEndCommand(action);
	}

	private void lookupValueToXML(String lookup, PValue value) {
		String psxType = toPsxTypeString(value.getType());
		
		// State tag
		psxState(lookup, psxType);
		// Value tag
		doResultOrValueTag("Value", value);
		// Close up the state tag.
		psxEndState();
	}
	
	private void doResultOrValueTag(String tag, PValue value) {
		if (value instanceof IntegerValue) {
			int integer = ((IntegerValue) value).getIntValue();
			psxResultOrValue(tag, integer);
		} else if (value instanceof RealValue) {
			double real = ((RealValue)value).getRealValue();
			psxResultOrValue(tag, real);
		} else {
			// This is a last resort. Strings and SPF don't really mix, and
			// this has caused issues in the past.
			psxResultOrValue(tag, value.toString());
		}

	}

	private void psxUpdateAck(Object node) {
		// This gets parsed in the listener
		// Should output <UpdateAck name="node" />
	}

	private void psxCommand(Object action, Object name, Object type) {
		// Should output <Commmand(action) name="name" type="type">
	}

	private void psxEndCommand(Object action) {
		// Should output </Command(action)>
	}

	private void psxResultOrValue(Object tag, Object result) {
		// Should output <tag>result</tag>
	}

	private void psxResultOrValue(Object tag, int result) {
		// Should output <tag>result</tag>
	}
	
	private void psxResultOrValue(Object tag, double result) {
		// Should output <tag>result</tag>
	}

	private void psxState(Object name, Object type) {
		// Should output <State name="name" type="type">
	}

	private void psxEndState() {
		// Should output </State>
	}

	private void psxParam(Object type, Object value) {
		// Should output <Param type="type">value</Param>
	}

	private void psxInitialStateEnd() {
		// Should output </InitialState><Script>
		
		// Incidentally, the listener should start out by doing this:
		// <PLEXILScript><InitialState>
		
		// And then whatever we said to do. Then, it should all be closed up
		// by doing </State></PLEXILScript>. 
	}

	private void psxSimultaneousStart() {
		// Should output <Simultaneous>
	}

	private void psxSimultaneousEnd() {
		// Should output </Simultaneous>
	}

	private String toPsxTypeString(PlexilType t) {
		switch (t) {
		case BOOLEAN: return "bool";
		case INTEGER: return "int";
		case NUMERIC:
		case REAL: return "real";
		case STRING: return "string";
		default:
			return t.toString().toLowerCase();
		}
	}

	private Object unwrapValue(PValue p) {
		// TODO: Find ways around casting to a string. It works, and there's a
		// reason why I did it, but strings are a bag of hurt in SPF. Add 
		// more things like psxResultOrValue(String, int), and more special
		// cases so that we can use them. 
		if (p.isUnknown()) {
			return "UNKNOWN";
		}
		return p.toString();
	}

}
