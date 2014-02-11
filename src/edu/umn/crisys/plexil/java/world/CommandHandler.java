package edu.umn.crisys.plexil.java.world;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.StandardValue;

public interface CommandHandler {

	public void setCommandHandle(CommandHandleState state);
	
	public void commandReturns(StandardValue value);
}
