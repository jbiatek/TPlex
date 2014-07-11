package edu.umn.crisys.plexil.runtime.psx.symbolic;

/**
 * Under symbolic analysis, a given search could essentially end at any point.
 * We could be in the middle of doing something when suddenly, the depth limit
 * has been reached and we are abruptly done executing. To match this behavior
 * in a standard JVM, we use this runtime exception to abruptly end
 * execution of the plan regardless of what else is going on. 
 * 
 * @author jbiatek
 *
 */
public class SimulatedBacktrackException extends RuntimeException {

	private static final long serialVersionUID = 558506208238071004L;

	public SimulatedBacktrackException() {
		super("In a symbolic environment, this is where this path would have ended. "
				+ "You shouldn't try to keep executing, but otherwise nothing bad happened.");
	}
}
