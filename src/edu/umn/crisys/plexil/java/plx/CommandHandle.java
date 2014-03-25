package edu.umn.crisys.plexil.java.plx;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.world.CommandHandler;

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
