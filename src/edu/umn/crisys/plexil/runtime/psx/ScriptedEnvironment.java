package edu.umn.crisys.plexil.runtime.psx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.UnknownBool;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.CommandAbortAck;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.ScriptEventVisitor;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;
import edu.umn.crisys.util.Pair;

public class ScriptedEnvironment implements ExternalWorld, ScriptEventVisitor<Object, Void> {
	
	public static boolean DEBUG = false;
	private boolean handlePlexilPrintCommands = false;
	private boolean throwExceptionIfDataIsMissing = false;

	private Map<FunctionCall, PValue> lookup = new HashMap<>();
	private List<Pair<CommandHandler,FunctionCall>> commandQueue = new ArrayList<>();
	private List<CommandHandler> commandAbortQueue = new ArrayList<>();
	private List<Pair<CommandHandler,FunctionCall>> unhandledCommands = new ArrayList<>();
    private List<UpdateHandler> updaters = new ArrayList<>();
    
    public void enableAutoResponseToPlexilPrintCommands() {
    	handlePlexilPrintCommands = true;
    }
    
    public void enableExceptionsForMissingLookupData() {
    	throwExceptionIfDataIsMissing = true;
    }
    
    public void applyEvent(Event e) {
    	e.accept(this, null);
    }
    
    public void reset() {
    	lookup.clear();
    	commandQueue.clear();
    	unhandledCommands.clear();
    	updaters.clear();
    }

	private Pair<CommandHandler,FunctionCall> findCommandThatSent(FunctionCall call) {
		// The reference implementation seems to actually pick the latest one 
		// that has come in. And it never removes them. 
		Pair<CommandHandler, FunctionCall> event = null;
		for (int i = commandQueue.size()-1; i >= 0; i--) {
			Pair<CommandHandler, FunctionCall> e = commandQueue.get(i);
			if (e.second.equals(call)) {
				event = e;
				break;
			}
		}
		if (event == null) {
			throw new RuntimeException("Could not find the command "+call);
		}
		return event;
	}
    
	private UpdateHandler findUpdate(String nodeName) {
        UpdateHandler correct = null;
        for (UpdateHandler node : updaters) {
            if (node.getNodeName().equals(nodeName)) {
                correct = node;
                break;
            }
        }
        if (correct == null) {
            throw new RuntimeException("Tried to find update from "
                    +nodeName+" but that node wasn't found");
        }
        return correct;
	}
	
	public Map<FunctionCall, PValue> getCurrentLookupMap() {
		return Collections.unmodifiableMap(lookup);
	}
	
	public List<Pair<CommandHandler,FunctionCall>> getCurrentCommandQueue() { 
		return Collections.unmodifiableList(commandQueue);
	}
	
	public List<Pair<CommandHandler,FunctionCall>> getUnhandledCommands() {
		return Collections.unmodifiableList(unhandledCommands);
	}
	
	public List<UpdateHandler> getCurrentUpdateQueue() {
		return Collections.unmodifiableList(updaters);
	}
	
	@Override
	public boolean stop() {
		return false;
	}
	
	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		if (DEBUG) {
			System.out.println("Update received: "+key+" = "+value+" from "+node);
		}
		updaters.add(node);
	}

	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		return lookup(stateName, args);
	}

	@Override
	public PValue lookupOnChange(PString stateName,
			PReal tolerance, PValue... args) {
		return lookup(stateName, args);
	}
	
	private PValue lookup(PString stateName, PValue...args ) {
		FunctionCall key = new FunctionCall(stateName.getString(), args);
		if (lookup.containsKey(key)) {
		    return lookup.get(key);
		} else if (stateName.getString().equals("time")) {
		    // The empty script isn't quite empty, it does have Lookup(time) = 0
			// by default. 
		    return RealValue.get(0.0);
		} 
		
		if (throwExceptionIfDataIsMissing) {
			throw new RuntimeException("Script has no response for "+key+
					", current lookup environment was "
					+lookup);
		} else {
			System.err.println("Warning: Script has no response for "+key+
					", returning UNKNOWN");
			return UnknownBool.get();
		}
		
	}

	@Override
	public void command(CommandHandler caller, PString name,
			PValue... args) {
		Pair<CommandHandler,FunctionCall> e = new Pair<CommandHandler, FunctionCall>(caller, 
				new FunctionCall(name.getString(), args));
        if (handlePlexilPrintCommands &&
        		(e.second.getName().equals("print")
                || e.second.getName().equals("pprint"))) {
        	// Handle utility commands right away
        	// (See PLEXIL's TestExternalInterface.cc, they do it the same way:
        	// immediately acknowledge with SUCCESS, and don't add it to the queue)
            e.first.setCommandHandle(CommandHandleState.COMMAND_SUCCESS);
        } else {
            commandQueue.add(e);
            unhandledCommands.add(e);
        }
	}
	
	@Override
	public void commandAbort(CommandHandler caller) {
		// Queue this handle for a future abort acknowledgement
		commandAbortQueue.add(caller);
	}

	
	/*
	 * Visitor methods for script events:
	 */

	@Override
	public Void visitCommandAck(CommandAck ack, Object param) {
		// Find the event
		if (DEBUG) {
			System.out.println("Acknowledging "+ack.getCall() + " with "+ack.getResult());
		}
		Pair<CommandHandler, FunctionCall> event = findCommandThatSent(ack.getCall());
		event.first.setCommandHandle(ack.getResult());
		// The reference implementation DOESN'T remove them. Yes, really.
		// But we maintain a saner list of things that haven't been dealt with,
		// so we don't have to consider this nonsense.
		unhandledCommands.remove(event);

		return null;
	}

	@Override
	public Void visitCommandReturn(CommandReturn ret, Object param) {
		if (DEBUG) {
			System.out.println("Returning "+ret.getValue()+" for "+ret.getCall());
		}
		
		findCommandThatSent(ret.getCall()).first.commandReturns(ret.getValue());
		
		return null;
	}

	@Override
	public Void visitCommandAbortAck(CommandAbortAck abort, Object param) {
		// Get the handle, first of all. 
		CommandHandler handle = findCommandThatSent(abort.getCall()).first;
		// Did we even hear about an abort?
		if ( ! commandAbortQueue.contains(handle)) {
			throw new RuntimeException("Command call "+abort.getCall()+" not waiting for an abort acknowledgement!");
		}
		// The exec does in fact remove these, you can't abort a call twice.
		commandAbortQueue.remove(handle);
		
		// And finally, the actual ack.
		handle.acknowledgeAbort();
		
		return null;
	}
	
	@Override
	public Void visitDelay(Delay d, Object param) {
		// Don't need to do anything.
		return null;
	}

	@Override
	public Void visitSimultaneous(Simultaneous sim, Object param) {
		for (Event e : sim.getEvents()) {
			applyEvent(e);
		}
		return null;
	}

	@Override
	public Void visitStateChange(StateChange lookup, Object param) {
		if (DEBUG) {
			System.out.println("Setting value of "+lookup.getLookup()+" to "+lookup.getValue());
		}
		this.lookup.put(lookup.getLookup(), lookup.getValue());
		
		return null;
	}

	@Override
	public Void visitUpdateAck(UpdateAck ack, Object param) {
    	UpdateHandler correct = findUpdate(ack.getNodeName());
    	
        correct.acknowledgeUpdate();
        this.updaters.remove(correct);

        return null;
	}




}
