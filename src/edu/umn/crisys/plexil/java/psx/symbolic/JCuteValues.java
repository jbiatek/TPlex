package edu.umn.crisys.plexil.java.psx.symbolic;

import cute.Cute;
import edu.umn.crisys.plexil.java.world.ValueSource;

/**
 * Returns symbolic values to a plan and sends PLEXILScript files to a listener
 * in JPF. Current limitations include:
 * 
 * <ul>
 * <li> Lookup arguments are ignored, so Lookup("temp", "sensor1") is the same
 * as Lookup("temp", "anything else").</li>
 * <li> Commands are only responded to once, with a return value if we've been
 * told that it should return something.</li>
 * <li> I don't know how to specify UNKNOWN values in PSX, so they aren't
 * ever created or returned. 
 * </ul>
 * @author jbiatek
 *
 */
public class JCuteValues implements ValueSource {
	
	
	public boolean symbolicBoolean(boolean makeMeSymbolic) {
		return Cute.input.Boolean();
	}
	
	public int symbolicInteger(int makeMeSymbolic) {
		return Cute.input.Integer();
	}
	
	public double symbolicDouble(double makeMeSymbolic) {
		return Cute.input.Double();
	}

	@Override
	public void continueOnlyIf(boolean expression) {
		Cute.Assume(expression);
	}

}
