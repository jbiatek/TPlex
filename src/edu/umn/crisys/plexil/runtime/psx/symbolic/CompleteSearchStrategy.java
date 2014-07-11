package edu.umn.crisys.plexil.runtime.psx.symbolic;

import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.FunctionCall;

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
