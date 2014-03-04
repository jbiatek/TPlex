import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.PInteger;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.java.world.CommandHandler;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.java.world.UpdateHandler;
import edu.umn.crisys.util.Pair;


/**
 * A driving environment with 4 sensors, "POSITION_0" through "POSITION_3".
 * By default, all 4 accurately return the current position, but they can be
 * set to fail (return arbitrary/symbolic values) using the 
 * setSensorIsBroken() method. 
 * @author jbiatek
 *
 */
public class SimpleDrivingEnvironment implements ExternalWorld {
	
	/**
	 * Track the current step number for logs, etc.
	 */
	int stepNumber = 0;
	
	/**
	 * This is the environment: a position that the sensors read. Obviously,
	 * a more complicated environment would have more than this.
	 */
	int position = 0;
	
	/**
	 * Flags to indicate whether a sensor is failing. 
	 */
	boolean[] failing = new boolean[]{false, false, false, false};
	
	/**
	 * Queue for incoming commands from the PLEXIL plan. 
	 */
	List<Pair<CommandHandler,Integer>> commandQueue = 
			new ArrayList<Pair<CommandHandler,Integer>>();
	
	/**
	 * Set whether the sensor given is broken or not. If it is broken,
	 * instead of reading the actual position, it could return anything.
	 * 
	 * @param sensorNumber
	 * @param isBroken
	 */
	public void setSensorIsBroken(int sensorNumber, boolean isBroken) {
		failing[sensorNumber] = isBroken;
	}
	
	@Override
	public void waitForNextEvent() {
		// This method is called between macro steps. 
		
		runtimeOracle();
		
		// Deal with the command queue. Since one of our properties is that 
		// the queue should never exceed 1, this for loop will run 0 or 1 time.
		for (Pair<CommandHandler, Integer> command : commandQueue) {
			// We'll just have every command successfully change the rover's
			// position. 
			position += command.second;
			command.first.setCommandHandle(CommandHandleState.COMMAND_SUCCESS);
		}
	
		// One more step completed.
		stepNumber++;
	}


	private void runtimeOracle() {
		// Throw an exception if any property has been violated
		
		if (commandQueue.size() > 1) {
			throw new RuntimeException("Multiple commands issued at once");
		}
	}


	/**
	 * Perform a Lookup("POSITION_N") and return the value that the sensor
	 * reads.
	 * 
	 * @param stateName
	 */
	private PValue lookup(PString stateName) {
		String state = stateName.getString();
		// Make sure this is something that we're expecting
		if ( ! state.startsWith("POSITION_")) {
			throw new RuntimeException("Unknown lookup");
		}
		// Get the sensor number
		int sensor = Integer.parseInt(state.replaceAll("POSITION_", ""));
		if (failing[sensor]) {
			// This sensor is failing, it could get anything.
			return getSymbolicIntegerValue();
		} else {
			// This sensor is operating correctly, return the real value.
			return IntegerValue.get(position);
		}
	
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		// Executed when the PLEXIL plan issues a Commmand.
		// Ensure that this is a command that we expect.
		if (name.getString().equals("Drive") && args.length == 1) {
			PInteger driveArg = (PInteger) args[0].castTo(PlexilType.INTEGER);
			if (driveArg.isUnknown()) {
				throw new RuntimeException("Was told to Drive(UNKNOWN)");
			}
			// Add the command to the queue, to be executed when the macro step ends.
			commandQueue.add(new Pair<CommandHandler, Integer>(caller, driveArg.getIntValue()));
		} else {
			throw new RuntimeException("Unknown command or wrong arguments: "
					+name+", "+Arrays.toString(args));
		}
	}

	/*
	 * Most of the stuff past this point is not very interesting. -------------
	 */
	
	
	/**
	 * Get a PInteger symbolically. Could be UNKNOWN, or any known integer.
	 * @return
	 */
	private PInteger getSymbolicIntegerValue() {
		if (symbolicBoolean(true)) {
			return IntegerValue.get(symbolicInt(0));
		} 
		return UnknownValue.get();
	}
	
	/**
	 * Injection point for symbolic boolean from SPF. Must be included in the
	 * JPF file's symbolic.method property. 
	 * @param makeMeSymbolic
	 * @return
	 */
	private boolean symbolicBoolean(boolean makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	/**
	 * Injection point for symbolic integer from SPF. Must be included in the
	 * JPF file's symbolic.method property. 
	 * @param makeMeSymbolic
	 * @return
	 */
	private int symbolicInt(int makeMeSymbolic) {
		return makeMeSymbolic;
	}

	
	@Override
	public boolean stop() {
		// Allow simulation to run indefinitely
		return false;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		// No updates expected
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

}
