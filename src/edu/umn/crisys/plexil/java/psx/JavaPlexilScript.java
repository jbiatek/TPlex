package edu.umn.crisys.plexil.java.psx;


import java.util.ArrayList;
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

public class JavaPlexilScript implements ExternalWorld {
	
	private Map<FunctionCall, PValue> lookup = 
			new HashMap<FunctionCall, PValue>();
	private List<ReceivedCommand> commandQueue = new ArrayList<ReceivedCommand>();
	
	private List<Event> events = new ArrayList<Event>();
    private List<UpdateHandler> updaters = new ArrayList<UpdateHandler>();
	
	public static interface Event {
		public void doEvent(JavaPlexilScript world);
	}
	
	private static class ReceivedCommand {
		private CommandHandler handler;
		private FunctionCall call;
		
		public ReceivedCommand(CommandHandler handler, FunctionCall call) {
			this.handler = handler;
			this.call = call;
			if (JavaPlan.DEBUG) {
				System.out.println("Command event created: "+handler+" "+call);
			}
		}

		public CommandHandler getHandler() {
			return handler;
		}

		public FunctionCall getCall() {
			return call;
		}

	}
	
	private static class StateChange implements Event {
		private FunctionCall call;
		private PValue value;
		
		public StateChange(FunctionCall call, PValue value) {
			this.call = call;
			this.value = value;
		}

		@Override
		public void doEvent(JavaPlexilScript world) {
			if (JavaPlan.DEBUG) {
				System.out.println("Setting value of "+call+" to "+value);
			}
			world.lookup.put(call, value);
		}
	}
	
	private static class CommandReturn implements Event {
		private FunctionCall call;
		private PValue value;
		
		public CommandReturn(FunctionCall call, PValue value) {
			this.call = call;
			this.value = value;
		}
		
		@Override
		public void doEvent(JavaPlexilScript world) {
			// Find the event
			if (JavaPlan.DEBUG) {
				System.out.println("Returning "+value+" for "+call);
			}
			ReceivedCommand event = null;
			for (ReceivedCommand e : world.commandQueue) {
				if (e.getCall().equals(call)) {
					event = e;
					break;
				}
			}
			if (event == null) {
				throw new RuntimeException("Could not find the command "+event);
			}
			event.getHandler().commandReturns(value);
		}
	}
	
	private static class CommandAck implements Event {
		private FunctionCall call;
		private CommandHandleState result;
		
		public CommandAck(FunctionCall call, CommandHandleState result) {
			this.call = call;
			this.result = result;
		}
		
		@Override
		public void doEvent(JavaPlexilScript world) {
			// Find the event
			if (JavaPlan.DEBUG) {
				System.out.println("Acknowledging "+call + " with "+result);
			}
			ReceivedCommand event = null;
			for (ReceivedCommand e : world.commandQueue) {
				if (e.getCall().equals(call)) {
					event = e;
					break;
				}
			}
			if (event == null) {
				System.err.println("WARNING: Could not find the command "+call);
				//world.events.add(0, this);
				return;
			}
			event.getHandler().setCommandHandle(result);
			world.commandQueue.remove(event);
		}
	}
	
	private static class UpdateAck implements Event {

	    private String nodeName;
	    
	    public UpdateAck(String nodeName) {
	        this.nodeName = nodeName;
	    }
	    
        @Override
        public void doEvent(JavaPlexilScript world) {
            UpdateHandler correct = null;
            for (UpdateHandler node : world.updaters) {
                if (node.getNodeName().equals(nodeName)) {
                    correct = node;
                    break;
                }
            }
            if (correct == null) {
                throw new RuntimeException("Tried to acknowledge update from "
                        +nodeName+" but that node wasn't found");
            }
            correct.acknowledgeUpdate();
            world.updaters.remove(correct);
        }
	    
	}
	
	private static class Delay implements Event {

        @Override
        public void doEvent(JavaPlexilScript world) {
            // I'm a delay! Look at me go!
        }
	    
	}
	
	private static class Simultaneous implements Event {
		private Event[] events;
		
		public Simultaneous(Event...events) {
			this.events = events;
		}

		@Override
		public void doEvent(JavaPlexilScript world) {
			for (Event e : events) {
				e.doEvent(world);
			}
		}
	}
	
	public void addEvent(Event e) {
		events.add(e);
	}
	
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
	    return new Delay();
	}
	
	public Simultaneous simultaneous(Event...events) {
		return new Simultaneous(events);
	}
	
	public void performAllEventsInQueue() {
		for (Event e : events) {
			e.doEvent(this);
		}
		events.clear();
	}
	
	public int getCommandQueueLength() {
		return commandQueue.size();
	}
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		if (events.size() > 0) {
			events.remove(0).doEvent(this);;
		}
		if (JavaPlan.DEBUG) {
			System.out.println("Events remaining in script: "+events.size());
		}
	}
	
	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		// Do the same thing as when quiesence is reached
		quiescenceReached(plan);
	}
	

	@Override
	public void endOfMicroStep(JavaPlan plan) {
		// Do nothing until the macro step ends.
	}

	@Override
	public boolean stop() {
		return events.size() == 0;
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
		    // Apparently, the empty script returns time as 0
		    return RealValue.get(0.0);
		} 
		
		System.err.println("Warning: Script has no response for "+key+
		", returning UNKNOWN");
		
		return UnknownValue.get();

	}

	@Override
	public void command(CommandHandler caller, PString name,
			PValue... args) {
		ReceivedCommand e = new ReceivedCommand(caller, 
				new FunctionCall(name.toString(), args));
		// Handle utility commands right away
        if (e.getCall().getName().equals("print")
                || e.getCall().getName().equals("pprint")) {
            e.getHandler().setCommandHandle(CommandHandleState.COMMAND_SUCCESS);
        } else {
            commandQueue.add(e);
        }
	}

}
