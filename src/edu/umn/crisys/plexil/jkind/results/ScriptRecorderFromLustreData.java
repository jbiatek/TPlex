package edu.umn.crisys.plexil.jkind.results;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.simulator.ILSimulator;
import edu.umn.crisys.plexil.il2lustre.LustreNamingConventions;
import edu.umn.crisys.plexil.il2lustre.ReverseTranslationMap;
import edu.umn.crisys.plexil.runtime.plx.JavaPlan;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PNumeric;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;
import edu.umn.crisys.plexil.runtime.world.UpdateHandler;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import jkind.lustre.values.Value;
import jkind.results.Signal;
import lustre.LustreTrace;

public class ScriptRecorderFromLustreData extends JavaPlexilScript {

	private LustreTrace lustreData;
	private ReverseTranslationMap map;
	
	private int lustreStepCounter = 0;
	private Set<FunctionCall> lookupsAlreadyRespondedTo = new HashSet<>();
	private Map<ILVariable,FunctionCall> lastKnownCommand = new HashMap<>();
	private Simultaneous currentStepEvents = new Simultaneous();
	
	public ScriptRecorderFromLustreData(LustreTrace cex, ReverseTranslationMap map) {
		this.lustreData = cex;
		this.map = map;
	}
	
	@Override
	public void endOfMicroStepBeforeCommit(JavaPlan plan) {
		lustreStepCounter++;
	}
	
	@Override
	public void endOfMacroStep(JavaPlan plan) {
		if (stop()) return;
		
		// We're running in a simulation, right? 
		if ( ! (plan instanceof ILSimulator)) {
			throw new RuntimeException("Script recorder only runs in an ILSimulator");
		}
		ILSimulator sim = (ILSimulator) plan;

		log("Macro step ended. Committing all generated events...");
		// All the events that occurred last step are done. Add them to the 
		// script we're building and move on. 
		this.addEvent(currentStepEvents.getCleanestEvent());
		currentStepEvents = new Simultaneous();
		lookupsAlreadyRespondedTo.clear();
		
		// Lookups will change during the step. 
		// We need to respond to commands that got responses in Lustre.
		log("Scanning Lustre data for command responses for the next macro step.");
		for (ILVariable cmdHandle : lastKnownCommand.keySet()) {
			NodeUID uid = cmdHandle.getNodeUID();
			log("Node "+uid+" is being examined.");
			// As long as it's not INACTIVE or WAITING, Lustre could 
			// respond to it. (Lustre isn't allowed to respond to SKIPPED or
			// precondition-failed nodes either, but in those cases there 
			// either won't be a command to respond to, or our "response" 
			// should just be setting an unknown handle to unknown again.)
			NodeState state = (NodeState) sim.eval(new GetNodeStateExpr(uid));
			if (state != NodeState.INACTIVE 
					&& state != NodeState.WAITING) {
				// Respond to this command with whatever Lustre said.
				FunctionCall thatNodesCall = lastKnownCommand.get(cmdHandle);
				CommandHandleState value = readCommandHandleFromTrace(cmdHandle); 
				log(uid+" handle is set to "+value+" in Lustre.");
				log("Its last known command was "+thatNodesCall);
				if (thatNodesCall == null) {
					log("So it is being skipped.");
					continue;
				}
				
				CommandAck ack = commandAck(value, thatNodesCall.getName(), thatNodesCall.getArgs());
				log("Event generated as a response: "+ack);
				
				getEnvironment().applyEvent(ack);
				currentStepEvents.addEvent(ack);
			} else {
				log("It's in state "+state+" so it is being skipped.");
			}
		}
	};

	private static void log(String s) {
		if (JavaPlan.DEBUG) {
			System.out.println("Recorder: "+s);
		}
	}
	
	@Override
	public boolean stop() {
		boolean ret = lustreStepCounter >= lustreData.getLength();
		if (ret) {
			log("Signalling stop because there is no more Lustre data");
		}
		return ret;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		throw new RuntimeException("Updates not implemented yet");
	}

	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		// We have lookups in terms of just the name, nothing else, but when 
		// this script runs, it'll need to include all these arguments too.
		if (lookupsAlreadyRespondedTo.contains(new FunctionCall(stateName, args))) {
			// We did this already. 
			return getEnvironment().lookupNow(stateName, args);
		} else {
			// We're doing this now, so don't do it again
			lookupsAlreadyRespondedTo.add(new FunctionCall(stateName, args));
		}
		
		log("Received request for lookup "+stateName+" with args "+join(args));
		
		PValue lustreValue = readLookupFromTrace(stateName.getString());
		log("Lustre says it should be "+lustreValue+".");
		// Pretend that we always had this value all along...
		StateChange stateChangeEvent = stateChange(lustreValue, stateName.getString(), args);
		getEnvironment().applyEvent(stateChangeEvent);
		currentStepEvents.addEvent(stateChangeEvent);
		
		return getEnvironment().lookupNow(stateName, args);
	}
	
	private PValue readLookupFromTrace(String stateName) {
		// Find the Lustre ID that this state is mapped to
		String id = map.getIdFromLookupName(stateName)
				.orElseThrow(() -> new RuntimeException("State "+stateName
						+" not found in Lustre map data"));
		return readValueFromTrace(id);
	}
	
	private CommandHandleState readCommandHandleFromTrace(ILVariable handle) {
		return (CommandHandleState) readValueFromTrace(
				LustreNamingConventions.getVariableId(handle));
	}
	
	private PValue readValueFromTrace(String lustreId) {
		Signal<Value> data = lustreData.getVariable(lustreId);
		if (data == null) {
			throw new NullPointerException("Signal for "+lustreId+" did not exist");
		}
		PValue value = JKindResultUtils.parseValue(data.getValue(lustreStepCounter), map);
		if (lustreId.endsWith("__value")) {
			// Check for unknown-ness first
			String knownId = lustreId.replaceFirst("value$", "isknown");
			Signal<Value> knownData = lustreData.getVariable(knownId);
			if (knownData == null) {
				throw new NullPointerException("Signal for "+knownId+" did not exist");
			}
			if (knownData.getValue(lustreStepCounter).toString()
					.equalsIgnoreCase("false")) {
				//Nope, actually this value is not known.
				return value.getType().getUnknown();
			} 
		}
		
		return value;
	}

	private static String join(Object[] args) {
		return "["+Arrays.stream(args).map(Object::toString)
				.collect(Collectors.joining(","))+"]";
	}
	
	@Override
	public PValue lookupOnChange(PString stateName, PNumeric tolerance, PValue... args) {
		// Same thing that LookupNow does
		return lookupNow(stateName, args);
	}

	public void specialCommand(SimpleVar simpleVar, CommandHandler handle, PString commandName, PValue... args) {
		log("Received command from node "+simpleVar.getNodeUID()
			+": name "+commandName+", args "+join(args));

		lastKnownCommand.put(simpleVar, 
				new FunctionCall(commandName.getString(), Arrays.asList(args)));
		// We have to tell our scripted environment about this too
		getEnvironment().command(handle, commandName, args);
	}
	
	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		throw new RuntimeException("Script recorder needs special support for "
				+ "commands, use specialCommand() instead!");
	}
	
	
}
