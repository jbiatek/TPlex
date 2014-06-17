package edu.umn.crisys.plexil.java.psx.symbolic;

import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.FunctionCall;

public interface ScriptDecisionMaker {
	
	public boolean shouldAckUpdate(ScriptedEnvironment env, UpdateHandler update);
	
	public boolean shouldAckCommand(ScriptedEnvironment env, FunctionCall call);
	public CommandHandleState getAckForCommand(FunctionCall call);
	
	public boolean doesCommandAlsoReturnValues(FunctionCall call);
	public PValue getReturnValueForCommand(FunctionCall call);
	
	public boolean shouldChangeLookup(ScriptedEnvironment env, FunctionCall lookup);
	public PValue getValueForLookup(ScriptedEnvironment env, FunctionCall lookup);

	public void endOfMacroStepNotification(boolean endedPrematurely);
}
