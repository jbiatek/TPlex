package edu.umn.crisys.plexil.runtime.world;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PValue;

public interface CommandHandler {

	public void setCommandHandle(CommandHandleState state);
	
	public void commandReturns(PValue value);
}
