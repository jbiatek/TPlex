package edu.umn.crisys.plexil.java.psx.symbolic;

import java.util.Random;

public class RandomValues implements ValueSource {

	private Random rand;
	
	public RandomValues(long seed) {
		rand = new Random(seed);
	}
	
	public RandomValues() {
		rand = new Random();
	}
	
	@Override
	public int symbolicInteger(int dummyValue) {
		return rand.nextInt(1000);
	}

	@Override
	public boolean symbolicBoolean(boolean dummyValue) {
		return rand.nextBoolean();
	}

	@Override
	public double symbolicDouble(double dummyValue) {
		return rand.nextDouble() * 1000;
	}

	@Override
	public void continueOnlyIf(boolean expression) {
		if ( ! expression) {
			throw new RuntimeException("Finished execution");
		}
	}

}
