package edu.umn.crisys.plexil.java.psx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.umn.crisys.util.Pair;

public class ScriptedEnvironment implements ExternalWorld {

	private Map<FunctionCall, PValue> lookup = new HashMap<FunctionCall, PValue>();
	private List<Pair<CommandHandler,FunctionCall>> commandQueue = 
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
	private List<Pair<CommandHandler,FunctionCall>> unhandledCommands =
			new ArrayList<Pair<CommandHandler,FunctionCall>>();
    private List<UpdateHandler> updaters = new ArrayList<UpdateHandler>();

    
    public void applyEvent(Event e) {
    	e.doEvent(this);
    }
    
    public void reset() {
    	lookup.clear();
    	commandQueue.clear();
    	updaters.clear();
    }

	public static interface Event {
		public void doEvent(ScriptedEnvironment world);
	}
	
	public static class StateChange implements Event {
		private FunctionCall call;
		private PValue value;
		
		public StateChange(FunctionCall call, PValue value) {
			this.call = call;
			this.value = value;
		}

		public FunctionCall getLookup() {
			return call;
		}
		
		public PValue getValue() {
			return value;
		}
		
		@Override
		public void doEvent(ScriptedEnvironment world) {
			if (JavaPlan.DEBUG) {
				System.out.println("Setting value of "+call+" to "+value);
			}
			world.lookup.put(call, value);
		}
		
		@Override
		public boolean equals(Object other) {
			if ( ! (other instanceof StateChange)) {
				return false;
			}
			StateChange o = (StateChange) other;
			return o.call.equals(this.call) && o.value.equals(this.value);
		}
		
		@Override
		public int hashCode() {
			return call.hashCode() + value.hashCode();
		}
	}
	
	public static class CommandReturn implements Event {
		private FunctionCall call;
		private PValue value;
		
		public CommandReturn(FunctionCall call, PValue value) {
			this.call = call;
			this.value = value;
		}
		
		public FunctionCall getCall() {
			return call;
		}
		
		public PValue getValue() {
			return value;
		}
		
		@Override
		public void doEvent(ScriptedEnvironment world) {
			if (JavaPlan.DEBUG) {
				System.out.println("Returning "+value+" for "+call);
			}
			
			world.findCommandThatSent(call).first.commandReturns(value);
		}
		
		@Override
		public boolean equals(Object other) {
			if ( ! (other instanceof CommandReturn)) {
				return false;
			}
			CommandReturn o = (CommandReturn) other;
			return o.call.equals(this.call) && o.value.equals(this.value);
		}
		
		@Override
		public int hashCode() {
			return call.hashCode() + value.hashCode();
		}

	}
	
	public static class CommandAck implements Event {
		private FunctionCall call;
		private CommandHandleState result;
		
		public CommandAck(FunctionCall call, CommandHandleState result) {
			this.call = call;
			this.result = result;
		}
		
		public FunctionCall getCall() {
			return call;
		}
		
		public CommandHandleState getResult() {
			return result;
		}
		
		@Override
		public void doEvent(ScriptedEnvironment world) {
			// Find the event
			if (JavaPlan.DEBUG) {
				System.out.println("Acknowledging "+call + " with "+result);
			}
			Pair<CommandHandler, FunctionCall> event = world.findCommandThatSent(call);
			event.first.setCommandHandle(result);
			// The reference implementation DOESN'T remove them. Yes, really.
			//world.commandQueue.remove(event);
			// But we maintain a saner list of things that haven't been dealt with,
			// so we don't have to consider this nonsense.
			world.unhandledCommands.remove(event);
		}
		
		@Override
		public boolean equals(Object other) {
			if ( ! (other instanceof CommandAck)) {
				return false;
			}
			CommandAck o = (CommandAck) other;
			return o.call.equals(this.call) && o.result.equals(this.result);
		}
		
		@Override
		public int hashCode() {
			return call.hashCode() + result.hashCode();
		}

	}
	
	public static class UpdateAck implements Event {

	    private String nodeName;
	    
	    public UpdateAck(String nodeName) {
	        this.nodeName = nodeName;
	    }
	    
	    public String getNodeName() {
	    	return nodeName;
	    }
	    
        @Override
        public void doEvent(ScriptedEnvironment world) {
        	UpdateHandler correct = world.findUpdate(nodeName);
        	
            correct.acknowledgeUpdate();
            world.updaters.remove(correct);
        }
	    
		@Override
		public boolean equals(Object other) {
			if ( ! (other instanceof UpdateAck)) {
				return false;
			}
			UpdateAck o = (UpdateAck) other;
			return o.nodeName.equals(this.nodeName);
		}
		
		@Override
		public int hashCode() {
			return nodeName.hashCode();
		}


        
	}
	
	public static class Delay implements Event {
		
		public static final Delay SINGLETON = new Delay();
		
		private Delay() {}

        @Override
        public void doEvent(ScriptedEnvironment world) {
            // I'm a delay! Look at me go!
        }
        
	}
	
	public static class Simultaneous implements Event {
		private List<Event> events;
		
		public Simultaneous(Event...events) {
			this(Arrays.asList(events));
		}

		public Simultaneous(List<Event> events) {
			this.events = events;
		}

		@Override
		public void doEvent(ScriptedEnvironment world) {
			for (Event e : events) {
				e.doEvent(world);
			}
		}
		
		public void addEvent(Event e) {
			events.add(e);
		}
		
		public List<Event> getEvents() {
			return events;
		}
		
		public Event getCleanestEvent() {
			if (events.isEmpty()) {
				return Delay.SINGLETON;
			}
			if (events.size() == 1) {
				return events.get(0);
			} 
			return this;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Simultaneous) {
				Simultaneous o = (Simultaneous) other;
				
				Set<Event> mySet = new HashSet<Event>(events);
				Set<Event> theirSet = new HashSet<Event>(o.events);
				
				return mySet.equals(theirSet);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return new HashSet<Event>(events).hashCode();
		}
		
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

}
