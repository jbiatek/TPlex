package edu.umn.crisys.plexil.runtime.psx.symbolic;

import java.util.HashMap;
import java.util.Map;

import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.script.ast.FunctionCall;

public abstract class SymbolicDecisionMaker implements ScriptDecisionMaker {
	
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
	
	private class BiasedBoolean implements ValueGenerator<PBoolean> {

		private double probabilityOfTrue;
		
		public BiasedBoolean(double probabilityOfTrue) {
			this.probabilityOfTrue = probabilityOfTrue;
		}
		
		
		@Override
		public PBoolean generateNewValue() {
			return BooleanValue.get(source.symbolicBoolean(true, probabilityOfTrue));
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
			int nondetIndex = source.symbolicInteger(0);
			// Just doing values[nondetIndex] doesn't work. SPF doesn't handle
			// array indexes the way it should.
			for (int i=0; i < values.length; i++) {
				if (i == nondetIndex) {
					return values[i];
				}
			}
			// Obviously, a value outside our bounds has been picked. Just go 
			// with the first one then, we have to pick one.
			return values[0];
		}
		
	}
	
	private class IncreasingReal implements ValueGenerator<PReal> {
		private PReal lastValue;
		
		public IncreasingReal(PReal init) {
			this.lastValue = init;
		}
		
		public IncreasingReal() {
			this(RealValue.get(0.0));
		}
		
		@Override
		public PReal generateNewValue() {
			return getRealGreaterEqualTo(lastValue.getRealValue());
		}
	}
	
	private class IncreasingInt implements ValueGenerator<PInteger> {
		private PInteger lastValue;
		
		public IncreasingInt(PInteger init) {
			this.lastValue = init;
		}
		
		public IncreasingInt() {
			this(IntegerValue.get(0));
		}
		
		@Override
		public PInteger generateNewValue() {
			return getIntGreaterEqualTo(lastValue.getIntValue());
		}
	}
	
	private Map<String,ValueGenerator<?>> lookupGenerators = new HashMap<String, ValueGenerator<?>>();
	private Map<String,ValueGenerator<CommandHandleState>> cmdResultGenerators = 
			new HashMap<String, ValueGenerator<CommandHandleState>>();
	private Map<String,ValueGenerator<?>> cmdReturnValueGenerators = new HashMap<String, ValueGenerator<?>>();
	private ValueSource source;

	public SymbolicDecisionMaker(ValueSource source) {
		this.source = source;
	}
	
	private ValueGenerator<?> getLookupGenerator(String lookup) {
		return lookupGenerators.get(lookup);
	}
	
	private ValueGenerator<CommandHandleState> getHandleGenerator(String cmd) {
		return cmdResultGenerators.get(cmd);
	}
	
	private boolean hasReturnValueGenerator(String cmd) {
		return cmdReturnValueGenerators.containsKey(cmd);
	}
	
	private ValueGenerator<?> getReturnValueGenerator(String cmd) {
		return cmdReturnValueGenerators.get(cmd);
	}
	
	boolean symbolicBoolean() {
		return source.symbolicBoolean(true);
	}
	
	private PInteger getIntGreaterEqualTo(int i) {
		int newSymb = source.symbolicInteger(0);
		if (newSymb > i) {
			return IntegerValue.get(newSymb);
		} else {
			return IntegerValue.get(i);
		}
	}
	
	private PReal getRealGreaterEqualTo(double f) {
		double newSymb = source.symbolicDouble(0);
		if (newSymb > f) {
			return RealValue.get(newSymb);
		} else {
			return RealValue.get(f);
		} 
	}
	
	private PValue getSymbolicPValueOfType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return new BooleanValue(source.symbolicBoolean(true));
		case INTEGER:
			return new IntegerValue(source.symbolicInteger(0));
		case REAL:
		case NUMERIC:
			return new RealValue(source.symbolicDouble(0.0));
		default:
			throw new RuntimeException("Type "+t+
					" does not have a symbolic method");
		}

	}
	

	/*
	 * Methods for configuring what to expect from the Plan:
	 */
	

	/**
	 * Create a Lookup which will return a symbolic value of the given
	 * type. 
	 * 
	 * @param lookup
	 * @param type
	 */
	public void addLookup(String lookup, PlexilType type) {
		lookupGenerators.put(lookup, new AnythingOfType<PValue>(type));
	}
	
	/**
	 * Create a Lookup which will return one of the given values symbolically.
	 * @param lookup
	 * @param values
	 */
	public void addLookup(String lookup, PValue... values) {
		lookupGenerators.put(lookup, new AnyOfThese<PValue>(values));
	}
	
	public void addLookupBooleanProb(String lookup, double probabilityOfTrue) {
		lookupGenerators.put(lookup, new BiasedBoolean(probabilityOfTrue));
	}
	
	public void addIncreasingLookup(String lookup, PlexilType type) {
		if (type == PlexilType.INTEGER) {
			lookupGenerators.put(lookup, new IncreasingInt());
		} else {
			lookupGenerators.put(lookup, new IncreasingReal());
		}
	}
	
	public void addIncreasingLookup(String lookup, double initialValue) {
		lookupGenerators.put(lookup, new IncreasingReal(RealValue.get(initialValue)));
	}
	
	public void addIncreasingLookup(String lookup, int initialValue) {
		lookupGenerators.put(lookup, new IncreasingInt(IntegerValue.get(initialValue)));
	}
	
	/**
	 * Create a Command which can respond with one of the given command handles.
	 * If none are given, any command handle is valid. 
	 * @param name
	 * @param states
	 */
	public void addCommand(String name, CommandHandleState... states) {
		cmdResultGenerators.put(name, new AnyOfThese<CommandHandleState>(states));
	}
	
	/**
	 * Create a Command which returns a symbolic value of the given type.
	 * @param name
	 * @param type
	 */
	public void addCommandReturn(String name, PlexilType type) {
		cmdReturnValueGenerators.put(name, new AnythingOfType<PValue>(type));
	}
	
	/**
	 * Create a Command which returns one of the given values. 
	 * @param name
	 * @param values
	 */
	public void addCommandReturn(String name, PValue...values) {
		cmdReturnValueGenerators.put(name, new AnyOfThese<PValue>(values));
	}
	
	@Override
	public final boolean doesCommandAlsoReturnValues(FunctionCall call) {
		return hasReturnValueGenerator(call.getName());
	}

	@Override
	public CommandHandleState getAckForCommand(FunctionCall call) {
		return getHandleGenerator(call.getName()).generateNewValue();
	}

	@Override
	public PValue getReturnValueForCommand(FunctionCall call) {
		return getReturnValueGenerator(call.getName()).generateNewValue();
	}

	@Override
	public PValue getValueForLookup(ScriptedEnvironment env, FunctionCall lookup) {
		return getLookupGenerator(lookup.getName()).generateNewValue();
	}

	@Override
	public void endOfMacroStepNotification(boolean endedPrematurely) {
		// Nothing necessarily needs to happen here
	}

	
}
