package edu.umn.crisys.plexil.script.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
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
	
	/**
	 * Write the given PlexilScript to the given file. 
	 * 
	 * @param f
	 * @param script
	 * @throws FileNotFoundException
	 */
	public static void writeToFile(File f, PlexilScript script) throws FileNotFoundException {
		writeToStream(new PrintWriter(f), script);
	}
	
	/**
	 * Write the given PlexilScript to the given stream. 
	 * 
	 * @param out
	 * @param events
	 */
	public static void writeToStream(PrintWriter out, PlexilScript script) {
		out.println("<PLEXILScript>");
		
		// Initial state first, if any
		if ( ! script.getInitialEvents().isEmpty()) {
			List<Event> cleanedInitial = new ArrayList<Event>(script.getInitialEvents());
			// Delete delay events, they're pointless in initial state
			cleanedInitial.removeIf(e -> e instanceof Delay);
			// Flatten out Simultaneous-es, the entire initial state is basically
			// one big simultaneous. 
			cleanedInitial = cleanedInitial.stream()
					.flatMap(e -> e instanceof Simultaneous ? 
							((Simultaneous)e).getEvents().stream() 
							: Stream.of(e))
					.collect(Collectors.toList());
			
			// Print out the events!
			out.println("<InitialState>");
			cleanedInitial.stream()
				.forEach((e) -> e.accept(SINGLETON, out));
			out.println("</InitialState>");
		}

		// Now for the main events. 
		out.println("<Script>");
		script.getMainEvents().forEach(e -> e.accept(SINGLETON, out));
		out.println("</Script>");
		out.println("</PLEXILScript>");
		
		out.flush();
		out.close();
	}
	
	private static void printParameterized(String tag, String resultTag, FunctionCall call, PValue result, PrintWriter out ) {
		if (result.isUnknown()) {
			System.out.println("Warning: tried to write unknown value into a script.");
			System.out.println("PSX doesn't support this, so it's being commented out. ");
			out.println("<!--");
		}
		String type = toPsxTypeString(result.getType());
		
		out.println("<"+tag+" name=\""+call.getName()+"\" type=\""+type+"\">");
		for (PValue arg : call.getArgs()) {
			printParam(arg, out);
		}
		printSimpleTag(resultTag, result, out);
		out.println("</"+tag+">");
		
		if (result.isUnknown()) {
			out.println("-->");
		}
	}
	
	private static void printParam(PValue arg, PrintWriter out) {
		String argStr = arg.toString();
		if (arg instanceof StringValue) {
			// Grab the actual string value for these, the toString() method
			// might include quotes or something
			argStr = ((StringValue) arg).getString();
		}
		out.println("    <Param type=\""+toPsxTypeString(arg.getType())+"\">"
				+argStr+"</Param>");
	}
	
	private static void printSimpleTag(String tagName, PValue arg, PrintWriter out) {
		out.println("    <"+tagName+">"+arg+"</"+tagName+">");
	}


	private static String toPsxTypeString(ExprType t) {
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
