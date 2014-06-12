package edu.umn.crisys.plexil.java.psx.symbolic;

import edu.umn.crisys.plexil.java.psx.FunctionCall;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.java.world.UpdateHandler;

public class CompleteSearchStrategy extends SymbolicDecisionMaker {

	public CompleteSearchStrategy(ValueSource source) {
		super(source);
	}

	@Override
	public boolean shouldAckUpdate(ScriptedEnvironment env, UpdateHandler update) {
		return symbolicBoolean();
	}

	@Override
	public boolean shouldAckCommand(ScriptedEnvironment env, FunctionCall call) {
		return symbolicBoolean();
	}

	@Override
	public boolean shouldChangeLookup(ScriptedEnvironment env,
			FunctionCall lookup) {
		return symbolicBoolean();
	}

}
