package edu.umn.crisys.plexil.runtime.psx;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PNumeric;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;

public class JavaPlexilScript implements ExternalWorld {
	
	private ScriptedEnvironment env = new ScriptedEnvironment();

	private List<Event> events = new ArrayList<Event>();
	private int eventCounter = 0;
	
	public JavaPlexilScript() {
		// Nothing to do, we're a perfectly valid empty PLEXILScript
	}
	
	public JavaPlexilScript(PlexilScript astScript) {
		// Initial event first, we hold it as 1 simultaneous over here.
		events.add(new Simultaneous(astScript.getInitialEvents()));
		// Then add the rest
		events.addAll(astScript.getMainEvents());
		
		// Finally, reset ourselves so that we're ready to go
		reset();
	}
	
    public void reset() {
    	env.reset();
    	
    	// Perform the initial event to get things started (this is 
    	// reference PLEXILScript's <InitialState> tag.)
    	env.applyEvent(events.get(0));
    	eventCounter = 1;
    }
    
    public List<Event> getEvents() {
    	return Collections.unmodifiableList(events);
    }
    
    public ScriptedEnvironment getEnvironment() {
    	return env;
    }
    
	public void addEvent(Event e) {
		events.add(e);
	}
	
	/*
	 * Convenience methods for creating events: 
	 */
	
	public StateChange stateChange(PValue value, 
			String name, PValue... args) {
		return new StateChange(new FunctionCall(name, args), value);
	}
	
	public CommandAck commandAck(CommandHandleState response,
			String name, PValue...args) {
		return new CommandAck(new FunctionCall(name, args), response);
	}
	
	public CommandReturn commandReturn(PValue value, 
			String name, PValue... args) {
		return new CommandReturn(new FunctionCall(name, args), value);
	}
	
	public UpdateAck updateAck(String nodeName) {
	    return new UpdateAck(nodeName);
	}
	
	public Delay delay() {
	    return Delay.SINGLETON;
	}
	
	public Simultaneous simultaneous(Event...events) {
		return new Simultaneous(events);
	}
	
	/*
	 * End convenience methods
	 */
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		if ( ! outOfEvents() ) {
			env.applyEvent(events.get(eventCounter));
			eventCounter++;
		}
	}
	
	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		// Do the same thing as when quiescence is reached
		quiescenceReached(plan);
	}
	

	@Override
	public boolean stop() {
		return outOfEvents();
	}
	
	public int eventsRemaining() {
		return events.size() - eventCounter;
	}
	
	public boolean outOfEvents() {
		return eventsRemaining() == 0;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		env.update(node, key, value);
	}

	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		return env.lookupNow(stateName, args);
	}

	@Override
	public PValue lookupOnChange(PString stateName, PNumeric tolerance,
			PValue... args) {
		return env.lookupOnChange(stateName, tolerance, args);
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		env.command(caller, name, args);
	}


}
