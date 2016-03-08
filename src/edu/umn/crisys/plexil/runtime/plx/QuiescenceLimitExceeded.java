package edu.umn.crisys.plexil.runtime.plx;

public class QuiescenceLimitExceeded extends RuntimeException {

	private static final long serialVersionUID = -4848765792496937282L;

	public QuiescenceLimitExceeded(int numSteps) {
		super("Quiesence took more than "+
                        numSteps+" microsteps, timed out");
	}

}
