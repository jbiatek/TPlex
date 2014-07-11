package edu.umn.crisys.plexil.runtime.psx.symbolic;

import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.FunctionCall;

public class MinimalEventStrategy extends SymbolicDecisionMaker {

	private boolean haventDoneAnythingYet = true;
	
	public MinimalEventStrategy(ValueSource source) {
		super(source);
	}

	@Override
	public boolean shouldAckUpdate(ScriptedEnvironment env, UpdateHandler update) {
		// Yeah sure, updates are fine. Who cares?
		return true;
	}

	@Override
	public boolean shouldAckCommand(ScriptedEnvironment env, FunctionCall call) {
		if (haventDoneAnythingYet) {
			// Maybe.
			return symbolicBoolean();
		}
		// No, we already did something.
		return false;
	}

	@Override
	public boolean shouldChangeLookup(ScriptedEnvironment env,
			FunctionCall lookup) {
		// We'd like everything to be defined, so if there isn't a value
		// already, we want to change it regardless.
		if ( ! env.getCurrentLookupMap().containsKey(lookup)) {
			return true;
		}
		// Change is optional then. 
		if (haventDoneAnythingYet) {
			// Maybe.
			return symbolicBoolean();
		}
		// No, we already did something.
		return false;
	}

	@Override
	public CommandHandleState getAckForCommand(FunctionCall call) {
		haventDoneAnythingYet = false;
		return super.getAckForCommand(call);
	}

	@Override
	public PValue getReturnValueForCommand(FunctionCall call) {
		haventDoneAnythingYet = false;
		return super.getReturnValueForCommand(call);
	}

	@Override
	public PValue getValueForLookup(ScriptedEnvironment env, FunctionCall lookup) {
		haventDoneAnythingYet = false;
		return super.getValueForLookup(env, lookup);
	}

	@Override
	public void endOfMacroStepNotification(boolean endedPrematurely) {
		haventDoneAnythingYet = true;
	}

}
