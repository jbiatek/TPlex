package edu.umn.crisys.plexil.runtime.psx.symbolic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.ScriptedEnvironment;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;
import edu.umn.crisys.plexil.runtime.world.ExternalWorld;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;
import edu.umn.crisys.util.Pair;

public class SymbolicScript implements ExternalWorld {
	
	
	private List<Event> currentStepEvents = new ArrayList<Event>();
	private List<Event> eventsPerformed = new ArrayList<Event>();
	private Set<FunctionCall> lookupsAlreadyCheckedThisStep = new HashSet<FunctionCall>();
	private ScriptedEnvironment env;
	private ScriptDecisionMaker delegate;
	private boolean didSomethingThisStep = false;
	
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
		if ( ! didSomethingThisStep && ! endedPrematurely) {
			System.out.println("Warning: The plan quiesced without asking for new inputs (other than possibly time).");
		}
		
		finalizePreviousStep();
		// Respond to commands and updates for the next step. Lookups get 
		// handled as they come in.
		respondToCommands();
		respondToUpdates();
	}

	private void finalizePreviousStep() {
		// Store all the Events we did.
		eventsPerformed.add(new Simultaneous(currentStepEvents).getCleanestEvent());
		// Then make a new one
		currentStepEvents = new ArrayList<Event>();
		didSomethingThisStep = false;
	}
	
	@Override
	public void endOfExecution(JavaPlan plan) {
		// We need to make sure that the last event gets saved.
		finalizePreviousStep();
	}

	@Override
	public boolean done() {
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
	public PValue lookupOnChange(PString stateName, PReal tolerance,
			PValue... args) {
		// Same deal as lookupNow.
		prepareForLookup(new FunctionCall(stateName.getString(), args));
		return env.lookupOnChange(stateName, tolerance, args);
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		env.update(node, key, value);
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		env.command(caller, name, args);
	}
	
	@Override
	public void commandAbort(CommandHandler caller) {
		env.commandAbort(caller);
	}

	private void applyEvent(Event e) {
		currentStepEvents.add(e);
		env.applyEvent(e);
	}
	
	private void respondToUpdates() {
		for (UpdateHandler update : env.getCurrentUpdateQueue()) {
			didSomethingThisStep = true;
			if (delegate.shouldAckUpdate(env, update)) {
				applyEvent(new UpdateAck(update.getNodeName()));
			}
		}
	}

	private void respondToCommands() {
		List<Event> eventsToApplyAfterLoop = new ArrayList<Event>();
		for (Pair<CommandHandler, FunctionCall> cmd : env.getUnhandledCommands()) {
			didSomethingThisStep = true;
			// Decide whether to respond to this command.
			FunctionCall call = cmd.second;
			if (delegate.shouldAckCommand(env, call)) {
				// If there are any, apparently return values should be first
				if (delegate.doesCommandAlsoReturnValues(call)) {
					PValue value = delegate.getReturnValueForCommand(call);
					eventsToApplyAfterLoop.add(new CommandReturn(call, value));
				}
				
				// Return a handle value to the command.
				CommandHandleState handle = delegate.getAckForCommand(call);
				eventsToApplyAfterLoop.add(new CommandAck(call, handle));
			}
		}
		for (Event e : eventsToApplyAfterLoop) {
			applyEvent(e);
		}
	}
	
	private void prepareForLookup(FunctionCall lookup) {
		if ( ! lookup.getName().equals("time")) {
			didSomethingThisStep = true;			
		}
		// If we already did this one, there's no need to prepare anything.
		if (lookupsAlreadyCheckedThisStep.contains(lookup)) return;
		
		// Create a state change event if the delegate wants us to
		if (delegate.shouldChangeLookup(env, lookup)) {
			// New event created
			PValue value = delegate.getValueForLookup(env, lookup);
			applyEvent(new StateChange(lookup, value));
		}
		// Make sure we don't touch this one until the next step
		lookupsAlreadyCheckedThisStep.add(lookup);
	}



}
