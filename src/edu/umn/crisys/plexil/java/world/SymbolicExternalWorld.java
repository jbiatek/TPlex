package edu.umn.crisys.plexil.java.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.crisys.plexil.java.psx.FunctionCall;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StandardValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.util.Pair;
import gov.nasa.jpf.vm.Verify;

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
	
	/**
	 * Updates that need acknowledging.
	 */
	private List<UpdateHandler> updateQueue = new ArrayList<UpdateHandler>();
	private List<Pair<CommandHandler,FunctionCall>> cmdQueue = 
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
	private Map<PString,PValue> currentLookupValues =
			new HashMap<PString, PValue>();
	private Set<PString> lookupsUsedThisStep = new HashSet<PString>();
	
	
	/**
	 * Types to return for lookups. They are not enumerated, so a symbolic
	 * value of the correct type is what should be used.
	 */
	private Map<String,PlexilType> lookupTypes = new HashMap<String, PlexilType>();
	/**
	 * Values to return for lookups. The user has specified that only the ones
	 * from this list should be used, so while the particular index can be
	 * chosen nondeterministically, one of these has to be what's actually used.
	 */
	private Map<String,List<PValue>> lookupEnums = new HashMap<String, List<PValue>>();
	
	private Set<String> increasingLookups = new HashSet<String>();
	
	/**
	 * Types to return from commands. They aren't enumerated, so a symbolic
	 * value of this type can be returned. 
	 */
	private Map<String,PlexilType> cmdReturnTypes = new HashMap<String, PlexilType>();
	/**
	 * Values to return for commands. Only the ones listed here should be used
	 * according to the user. 
	 */
	private Map<String,List<PValue>> cmdReturnEnums = new HashMap<String, List<PValue>>();
	/**
	 * Command handle statuses to give back to the command. If the list is
	 * empty, assume that they're all valid.
	 */
	private Map<String,CommandHandleState[]> cmdResultEnums = 
			new HashMap<String, CommandHandleState[]>();
	
	
	
	private int currentStep = -1;

	private boolean symbolicBoolean(boolean makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private int symbolicInt(int makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private double symbolicDouble(double makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private String symbolicString(String makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private PValue getSymbolicPValueOfType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return BooleanValue.get(symbolicBoolean(true));
		case INTEGER:
			return IntegerValue.get(symbolicInt(0));
		case REAL:
		case NUMERIC:
			return RealValue.get(symbolicDouble(0.0));
		case STRING:
			return StringValue.get(symbolicString(""));
		default:
			throw new RuntimeException("Type "+t+
					" does not have a symbolic method");
		}

	}
	
	
	private int symbolicIntIndex(int maximum) {
		int symb = symbolicInt(0);
		Verify.ignoreIf(symb >= maximum || symb < 0);
		return symb;
	}
	
	/**
	 * Create a Lookup which will return a symbolic value of the given
	 * type. 
	 * 
	 * @param lookup
	 * @param type
	 */
	public void addLookup(String lookup, PlexilType type) {
		lookupTypes.put(lookup, type);
		regenerateLookup(lookup);
	}
	
	/**
	 * Create a Lookup which will return one of the given values symbolically.
	 * @param lookup
	 * @param values
	 */
	public void addLookup(String lookup, PValue... values) {
		lookupEnums.put(lookup, Arrays.asList(values));
		regenerateLookup(lookup);
	}
	
	public void addIncreasingLookup(String lookup, PlexilType type) {
		if ( ! type.isNumeric()) {
			throw new RuntimeException("Cannot create non-numeric increasing lookup");
		}
		addLookup(lookup, type);
		increasingLookups.add(lookup);
	}
	
	/**
	 * Create a Command which can respond with one of the given command handles.
	 * If none are given, any command handle is valid. 
	 * @param name
	 * @param states
	 */
	public void addCommand(String name, CommandHandleState... states) {
		cmdResultEnums.put(name, states);
	}
	
	/**
	 * Create a Command which returns a symbolic value of the given type.
	 * @param name
	 * @param type
	 */
	public void addCommandReturn(String name, PlexilType type) {
		cmdReturnTypes.put(name, type);
	}
	
	/**
	 * Create a Command which returns one of the given values. 
	 * @param name
	 * @param values
	 */
	public void addCommandReturn(String name, PValue...values) {
		cmdReturnEnums.put(name, Arrays.asList(values));
	}
	
	private void respondToUpdate(UpdateHandler u) {
		u.acknowledgeUpdate();
		psxUpdateAck(u.getNodeName());
	}
	
	private void regenerateLookup(String lookup) {
		PValue oldValueCaptured = currentLookupValues.get(lookup);
		if (lookupEnums.containsKey(lookup)) {
			// We have to pick one of these values.
			List<PValue> choices = lookupEnums.get(lookup);
			int valueToChoose = symbolicIntIndex(choices.size());
			changeLookup(lookup, choices.get(valueToChoose));
		} else {
			// We just need something symbolic that's the right type.
			// TODO: Add XML support for UNKNOWN and make that a 
			// possibility here. 
			if (lookupTypes.get(lookup) == null) {
				throw new RuntimeException("Type not given for lookup "+lookup);
			}
			PValue newValue = getSymbolicPValueOfType(lookupTypes.get(lookup));
			changeLookup(lookup, newValue);
		}
		
		if (oldValueCaptured != null && increasingLookups.contains(lookup)) {
			// Need to make sure our new value is bigger, if it is new.
			PNumeric oldValNumeric = (PNumeric) oldValueCaptured;
			PNumeric newValNumeric = (PNumeric) currentLookupValues.get(lookup);
			Verify.ignoreIf(oldValNumeric.gt(newValNumeric).isTrue());
		}

	}
	
	private void respondToCommand(CommandHandler handler, FunctionCall call) {
		// Does this command return a value? And if so, is it just a type or
		// are the values enumerated for us?
		if (cmdReturnEnums.containsKey(call.getName())) {
			// Enumerated. Pick one and return it.
			List<PValue> choices = cmdReturnEnums.get(call.getName());
			int choice = symbolicIntIndex(choices.size());
			returnValueToCommand(handler, call, choices.get(choice));
		} else if (cmdReturnTypes.containsKey(call.getName())) {
			// Just a type, so get one and return it. 
			returnValueToCommand(handler, call, 
					getSymbolicPValueOfType(cmdReturnTypes.get(call.getName())));
		}
		
		// Now for the CommandAck. By default, we'll choose any handle.
		CommandHandleState[] choices = CommandHandleState.values();
		if (cmdResultEnums.containsKey(call.getName())
				&& cmdResultEnums.get(call.getName()) != null) {
			// Ah, it's enumerated. Let's only pick from these.
			choices = cmdResultEnums.get(call.getName());
		}
		// Now make the choice and go.
		int choice = symbolicIntIndex(choices.length);
		commandAck(handler, call, choices[choice]);

	}
	
	private void returnValueToCommand(CommandHandler handler, FunctionCall call, PValue v) {
		handler.commandReturns((StandardValue) v);
		constructCommandXML(call, v, "");
	}
	
	private void commandAck(CommandHandler handler, FunctionCall call, CommandHandleState status) {
		handler.setCommandHandle(status);
		constructCommandXML(call, status, "Ack");
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
		if (p.isUnknown()) {
			return "UNKNOWN";
		}
		else if(p instanceof CommandHandleState) {
			return p.toString();
		}
		StandardValue v = (StandardValue) p;
		// It has to be a string because SymbolicSequenceListener doesn't
		// currently unwrap Booleans to primitives. So we end up getting
		// something useless like "java.lang.Integer@234". 
		return v.asNativeJava().toString();
	}
	
	private void changeLookup(String lookup, PValue v) {
		PlexilType t = v.getType();
		if (t==PlexilType.UNKNOWN) {
			t = lookupTypes.get(lookup);
		}
		currentLookupValues.put(StringValue.get(lookup), v);
		psxState(lookup, toPsxTypeString(t));
		psxResultOrValue("Value", unwrapValue(v));
		psxEndState();
	}
	
	

	@Override
	public void waitForNextEvent() {
		if (currentStep == -1) {
			// Before we make anything new, make sure to close the InitialState
			// tag.
			psxInitialStateEnd();
		}
		currentStep++;
		psxSimultaneousStart();
		
		for (UpdateHandler handler : updateQueue) {
			if (symbolicBoolean(true)) {
				respondToUpdate(handler);
			}
		}
		updateQueue.clear();

		Iterator<Pair<CommandHandler, FunctionCall>> iter = cmdQueue.iterator();
		while (iter.hasNext()) {
			Pair<CommandHandler, FunctionCall> pair = iter.next();
			if (symbolicBoolean(true)) {
				respondToCommand(pair.first, pair.second);
				iter.remove();
			}
		}
		
		// Give any lookup that was actually used a chance to change.
		for (PString lookup : lookupsUsedThisStep) {
			if (symbolicBoolean(true)) {
				regenerateLookup(lookup.getString());
			}
		}
		lookupsUsedThisStep.clear();
		
		psxSimultaneousEnd();
	}

	@Override
	public boolean stop() {
		// SPF will backtrack when depth is reached. 
		return false;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		updateQueue.add(node);
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
		lookupsUsedThisStep.add(stateName);
		return currentLookupValues.get(stateName);
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		if (name.isUnknown()) {
			throw new RuntimeException("Called UNKNOWN function.");
		}
		cmdQueue.add(new Pair<CommandHandler, FunctionCall>(caller, 
				new FunctionCall(name.getString(), args)));
	}

}
