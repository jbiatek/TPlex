package edu.umn.crisys.plexil.script.translator;

import java.io.PrintWriter;
import java.util.List;

import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.ScriptEventVisitor;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;

public class ScriptToXML implements ScriptEventVisitor<PrintWriter,Void> {
	
	/**
	 * Write the given events to the given stream. It is assumed that the first
	 * event is the InitialState, and the rest are the events that will be 
	 * executed one at a time. If you don't want an InitialState, simply
	 * put a Delay event there to represent "nothing". 
	 * 
	 * @param out
	 * @param events
	 */
	public static void writeToStream(PrintWriter out, List<Event> events) {
		out.println("<PLEXILScript>");
		boolean startedScriptTag = false;
		
		
		for (int i=0; i<events.size(); i++) {
			Event e = events.get(i);
			
			if (i==0) {
				// This is the initial state. It gets some special treatment.
				if (e instanceof Delay) continue;
				else if (e instanceof Simultaneous) {
					out.println("<InitialState>");
					for (Event child : ((Simultaneous) e).getEvents()) {
						child.accept(SINGLETON, out);
					}
					out.println("</InitialState>");
				} else {
					out.println("<InitialState>");
					e.accept(SINGLETON, out);
					out.println("</InitialState>");
				}
			} else {
				// Not the initial state. Basically just print the event.
				if (i == 1){
					out.println("<Script>");
					startedScriptTag = true;
				}
				
				e.accept(SINGLETON, out);
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

	
	private static final ScriptToXML SINGLETON = new ScriptToXML();
	private ScriptToXML() {}

	@Override
	public Void visitCommandAck(CommandAck ack, PrintWriter out) {
		printParameterized("CommandAck", "Result", ack.getCall(), ack.getResult(), out);
		return null;
	}

	@Override
	public Void visitCommandReturn(CommandReturn ret, PrintWriter out) {
		printParameterized("Command", "Result", ret.getCall(), ret.getValue(), out);
		return null;
	}

	@Override
	public Void visitDelay(Delay d, PrintWriter out) {
		out.println("<Delay />");
		return null;
	}

	@Override
	public Void visitSimultaneous(Simultaneous sim, PrintWriter out) {
		out.println("<Simultaneous>");
		for (Event child : sim.getEvents()) {
			child.accept(this, out);
		}
		out.println("</Simultaneous>");
		return null;
	}

	@Override
	public Void visitStateChange(StateChange lookup, PrintWriter out) {
		printParameterized("State", "Value", lookup.getLookup(), lookup.getValue(), out);
		return null;
	}

	@Override
	public Void visitUpdateAck(UpdateAck ack, PrintWriter out) {
		out.println("<UpdateAck name=\""+ack.getNodeName()+"\" />");
		return null;
	}

}
