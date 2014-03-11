package edu.umn.crisys.plexil.java.world;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PValue;

public interface CommandHandler {

	public void setCommandHandle(CommandHandleState state);
	
	public void commandReturns(PValue value);
}
