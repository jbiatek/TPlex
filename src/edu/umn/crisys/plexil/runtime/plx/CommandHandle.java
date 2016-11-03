package edu.umn.crisys.plexil.runtime.plx;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;

public class CommandHandle implements CommandHandler {

    private SimpleCurrentNext<CommandHandleState> handleValue;
    private SimpleCurrentNext<Boolean> abortAck;
    
    public CommandHandle(SimpleCurrentNext<CommandHandleState> wrappedValue,
    		SimpleCurrentNext<Boolean> abortAck) {
    	this.handleValue = wrappedValue;
    	this.abortAck = abortAck;
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

	@Override
	public void acknowledgeAbort() {
		abortAck.setNext(true);
		abortAck.commit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abortAck == null) ? 0 : abortAck.hashCode());
		result = prime * result
				+ ((handleValue == null) ? 0 : handleValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandHandle other = (CommandHandle) obj;
		if (abortAck == null) {
			if (other.abortAck != null)
				return false;
		} else if (!abortAck.equals(other.abortAck))
			return false;
		if (handleValue == null) {
			if (other.handleValue != null)
				return false;
		} else if (!handleValue.equals(other.handleValue))
			return false;
		return true;
	}
    
	
	
}
