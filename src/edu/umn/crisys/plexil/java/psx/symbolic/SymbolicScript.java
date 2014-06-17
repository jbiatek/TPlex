package edu.umn.crisys.plexil.java.psx.symbolic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.psx.FunctionCall;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.CommandAck;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.CommandReturn;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.Delay;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.Event;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.Simultaneous;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.StateChange;
import edu.umn.crisys.plexil.java.psx.ScriptedEnvironment.UpdateAck;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.world.CommandHandler;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.java.world.UpdateHandler;
import edu.umn.crisys.util.Pair;

public class SymbolicScript implements ExternalWorld {
	
	
	private List<Event> currentStepEvents = new ArrayList<Event>();
	private List<Event> eventsPerformed = new ArrayList<Event>();
	private Set<FunctionCall> lookupsAlreadyCheckedThisStep = new HashSet<FunctionCall>();
	private ScriptedEnvironment env;
	private ScriptDecisionMaker delegate;
	
	public SymbolicScript(ScriptDecisionMaker delegate) {
		this(new ScriptedEnvironment(), delegate);
	}
	
	public SymbolicScript(ScriptedEnvironment env, ScriptDecisionMaker delegate) {
		this.env = env;
		this.delegate = delegate;
	}
	
	public List<Event> getEventsPerformed() {
		return eventsPerformed;
	}
	
	public void writeToXML(PrintWriter out) {
		out.println("<PLEXILScript>");
		boolean startedScriptTag = false;
		
		
		for (int i=0; i<eventsPerformed.size(); i++) {
			Event e = eventsPerformed.get(i);
			
			if (i==0) {
				// This is the initial state. It gets some special treatment.
				if (e instanceof Delay) continue;
				else if (e instanceof Simultaneous) {
					out.println("<InitialState>");
					for (Event child : ((Simultaneous) e).getEvents()) {
						printEvent(child, out);
					}
					out.println("</InitialState>");
				} else {
					out.println("<InitialState>");
					printEvent(e, out);
					out.println("</InitialState>");
				}
			} else {
				// Not the initial state. Basically just print the event.
				if (i == 1){
					out.println("<Script>");
					startedScriptTag = true;
				}
				
				printEvent(e, out);
			}
		}
		
		if (startedScriptTag) {
			out.println("</Script>");
		} else {
			// They are mandatory, so stick an empty one in.
			out.println("<Script />");
		}

		out.println("</PLEXILScript>");
		out.flush();
		out.close();
	}
	
	private static void printEvent(Event e, PrintWriter out) {
		if (e instanceof StateChange) {
			StateChange lookup = (StateChange) e;
			printParameterized("State", "Value", lookup.getLookup(), lookup.getValue(), out);
		} else if (e instanceof CommandAck) {
			CommandAck ack = (CommandAck) e;
			printParameterized("CommandAck", "Result", ack.getCall(), ack.getResult(), out);
		} else if (e instanceof CommandReturn) {
			CommandReturn ret = (CommandReturn) e;
			printParameterized("Command", "Result", ret.getCall(), ret.getValue(), out);
		} else if (e instanceof UpdateAck) {
			out.println("<UpdateAck name=\""+((UpdateAck)e).getNodeName()+"\" />");
		} else if (e instanceof Delay) {
			out.println("<Delay />");
		} else if (e instanceof Simultaneous) {
			out.println("<Simultaneous>");
			for (Event child : ((Simultaneous) e).getEvents()) {
				printEvent(child, out);
			}
			out.println("</Simultaneous>");
		}
	}
	
	private static void printParameterized(String tag, String resultTag, FunctionCall call, PValue result, PrintWriter out ) {
		String type = toPsxTypeString(result.getType());
		
		out.println("<"+tag+" name=\""+call.getName()+"\" type=\""+type+"\">");
		for (PValue arg : call.getArgs()) {
			printParam(arg, out);
		}
		printSimpleTag(resultTag, result, out);
		out.println("</"+tag+">");
	}
	
	private static void printParam(PValue arg, PrintWriter out) {
		out.println("    <Param type=\""+toPsxTypeString(arg.getType())+"\">"
				+arg.toString()+"</Param>");
	}
	
	private static void printSimpleTag(String tagName, PValue arg, PrintWriter out) {
		out.println("    <"+tagName+">"+arg+"</"+tagName+">");
	}

	private static String toPsxTypeString(PlexilType t) {
		switch (t) {
		case BOOLEAN: return "bool";
		case INTEGER: return "int";
		case NUMERIC:
		case REAL: return "real";
		case STRING: 
		case COMMAND_HANDLE: return "string";
		
		default:
			return t.toString().toLowerCase();
		}
	}
	
	@Override
	public void quiescenceReached(JavaPlan plan) {
		endOfMacroStep(plan, false);
	}

	@Override
	public void prematureEndOfMacroStep(JavaPlan plan) {
		endOfMacroStep(plan, true);
	}

	private void endOfMacroStep(JavaPlan plan, boolean endedPrematurely) {
		lookupsAlreadyCheckedThisStep.clear();
		delegate.endOfMacroStepNotification(endedPrematurely);
		// Store all the Events we did.
		eventsPerformed.add(new Simultaneous(currentStepEvents).getCleanestEvent());
		// Then make a new one
		currentStepEvents = new ArrayList<Event>();
		
		// Respond to commands and updates for the next step. Lookups get 
		// handled as they come in.
		respondToCommands();
		respondToUpdates();
	}

	@Override
	public boolean stop() {
		// We can keep making stuff up forever, baby.
		return false;
	}


	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		// We sneak in and do some stuff before returning the response.
		prepareForLookup(new FunctionCall(stateName.getString(), args));
		return env.lookupNow(stateName, args);
	}


	@Override
	public PValue lookupOnChange(PString stateName, PNumeric tolerance,
			PValue... args) {
		// Same deal as lookupNow.
		prepareForLookup(new FunctionCall(stateName.getString(), args));
		return env.lookupOnChange(stateName, tolerance, args);
	}

	@Override
	public void endOfMicroStep(JavaPlan plan) {
		// Do nothing at all.
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		env.update(node, key, value);
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		env.command(caller, name, args);
	}
	
	private void applyEvent(Event e) {
		currentStepEvents.add(e);
		env.applyEvent(e);
	}
	
	private void respondToUpdates() {
		for (UpdateHandler update : env.getCurrentUpdateQueue()) {
			if (delegate.shouldAckUpdate(env, update)) {
				applyEvent(new ScriptedEnvironment.UpdateAck(update.getNodeName()));
			}
		}
	}

	private void respondToCommands() {
		List<Event> eventsToApplyAfterLoop = new ArrayList<Event>();
		for (Pair<CommandHandler, FunctionCall> cmd : env.getUnhandledCommands()) {
			// Decide whether to respond to this command.
			FunctionCall call = cmd.second;
			if (delegate.shouldAckCommand(env, call)) {
				// If there are any, apparently return values should be first
				if (delegate.doesCommandAlsoReturnValues(call)) {
					PValue value = delegate.getReturnValueForCommand(call);
					eventsToApplyAfterLoop.add(new ScriptedEnvironment.CommandReturn(call, value));
				}
				
				// Return a handle value to the command.
				CommandHandleState handle = delegate.getAckForCommand(call);
				eventsToApplyAfterLoop.add(new ScriptedEnvironment.CommandAck(call, handle));
			}
		}
		for (Event e : eventsToApplyAfterLoop) {
			applyEvent(e);
		}
	}
	
	private void prepareForLookup(FunctionCall lookup) {
		// If we already did this one, there's no need to prepare anything.
		if (lookupsAlreadyCheckedThisStep.contains(lookup)) return;
		
		// Create a state change event if the delegate wants us to
		if (delegate.shouldChangeLookup(env, lookup)) {
			// New event created
			PValue value = delegate.getValueForLookup(env, lookup);
			applyEvent(new ScriptedEnvironment.StateChange(lookup, value));
		}
		// Make sure we don't touch this one until the next step
		lookupsAlreadyCheckedThisStep.add(lookup);
	}



}
