package edu.umn.crisys.plexil.runtime.plx;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;

public class CommandHandle implements CommandHandler {

    private SimpleCurrentNext<CommandHandleState> handleValue;
    
    public CommandHandle(SimpleCurrentNext<CommandHandleState> wrappedValue) {
    	this.handleValue = wrappedValue;
    }
    
    @Override
    public void setCommandHandle(CommandHandleState state) {
        handleValue.setNext(state);
        handleValue.commit();
    }

    @Override
    public void commandReturns(PValue value) {
        throw new RuntimeException("A target for returning a value from this Command was not specified");
    }
    
}
