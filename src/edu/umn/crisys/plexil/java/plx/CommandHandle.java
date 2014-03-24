package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.world.CommandHandler;

public class CommandHandle implements CommandHandler {

    private CommandHandleState currentHandle = CommandHandleState.UNKNOWN;
    
    public CommandHandleState getCommandHandle() {
        return currentHandle;
    }
    
    @Override
    public void setCommandHandle(CommandHandleState state) {
        currentHandle = state;
    }

    @Override
    public void commandReturns(PValue value) {
        
    }
    
    public void reset() {
        currentHandle = CommandHandleState.UNKNOWN;
    }
    
}
