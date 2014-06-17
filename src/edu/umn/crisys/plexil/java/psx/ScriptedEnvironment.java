package edu.umn.crisys.plexil.java.psx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.java.world.CommandHandler;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.java.world.UpdateHandler;
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

	private Map<FunctionCall, PValue> lookup = new HashMap<FunctionCall, PValue>();
	private List<Pair<CommandHandler,FunctionCall>> commandQueue = 
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
	private List<Pair<CommandHandler,FunctionCall>> unhandledCommands =
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
    private List<UpdateHandler> updaters = new ArrayList<UpdateHandler>();
    
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
				//break;
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
	public void quiescenceReached(JavaPlan plan) {
		// Don't do anything, that's for others to figure out.
	}

	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		// Don't do anything, that's for others to figure out.
	}

	@Override
	public final void endOfMicroStep(JavaPlan plan) {
		// Don't do anything, and nothing else should do anything either really.
	}

	@Override
	public boolean stop() {
		return false;
	}
	
	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		if (JavaPlan.DEBUG) {
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
			PNumeric tolerance, PValue... args) {
		return lookup(stateName, args);
	}
	
	private PValue lookup(PString stateName, PValue...args ) {
		FunctionCall key = new FunctionCall(stateName.toString(), args);
		if (lookup.containsKey(key)) {
		    return lookup.get(key);
		} else if (stateName.getString().equals("time")) {
		    // The empty script isn't quite empty, it does have Lookup(time) = 0
			// by default. 
		    return RealValue.get(0.0);
		} 
		
		System.err.println("Warning: Script has no response for "+key+
		", returning UNKNOWN");
		
		return UnknownValue.get();
	}

	@Override
	public void command(CommandHandler caller, PString name,
			PValue... args) {
		Pair<CommandHandler,FunctionCall> e = new Pair<CommandHandler, FunctionCall>(caller, 
				new FunctionCall(name.toString(), args));
		// Handle utility commands right away
		// (See PLEXIL's TestExternalInterface.cc, they do it the same way:
		// immediately acknowledge with SUCCESS, and don't add it to the queue)
        if (e.second.getName().equals("print")
                || e.second.getName().equals("pprint")) {
            e.first.setCommandHandle(CommandHandleState.COMMAND_SUCCESS);
        } else {
            commandQueue.add(e);
            unhandledCommands.add(e);
        }
	}
	
	
	/*
	 * Visitor methods for script events:
	 */

	@Override
	public Void visitCommandAck(CommandAck ack, Object param) {
		// Find the event
		if (JavaPlan.DEBUG) {
			System.out.println("Acknowledging "+ack.getCall() + " with "+ack.getResult());
		}
		Pair<CommandHandler, FunctionCall> event = findCommandThatSent(ack.getCall());
		event.first.setCommandHandle(ack.getResult());
		// The reference implementation DOESN'T remove them. Yes, really.
		//world.commandQueue.remove(event);
		// But we maintain a saner list of things that haven't been dealt with,
		// so we don't have to consider this nonsense.
		unhandledCommands.remove(event);
		
		return null;
	}

	@Override
	public Void visitCommandReturn(CommandReturn ret, Object param) {
		if (JavaPlan.DEBUG) {
			System.out.println("Returning "+ret.getValue()+" for "+ret.getCall());
		}
		
		findCommandThatSent(ret.getCall()).first.commandReturns(ret.getValue());
		
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
		if (JavaPlan.DEBUG) {
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
