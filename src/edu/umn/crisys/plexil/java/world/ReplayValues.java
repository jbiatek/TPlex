package edu.umn.crisys.plexil.java.world;

import java.util.ArrayList;
import java.util.List;

public class ReplayValues implements ValueSource {
	
	private List<Integer> integers = new ArrayList<Integer>();
	private List<Boolean> booleans = new ArrayList<Boolean>();
	private List<Double> doubles = new ArrayList<Double>();
	
	public void addInteger(int integer) {
		integers.add(integer);
	}

	public void addBoolean(boolean bool) {
		booleans.add(bool);
	}
	
	public void addDouble(double dubs) {
		doubles.add(dubs);
	}
	
	@Override
	public int symbolicInteger(int dummyValue) {
		return integers.remove(0);
	}

	@Override
	public boolean symbolicBoolean(boolean dummyValue) {
		return booleans.remove(0);
	}

	@Override
	public double symbolicDouble(double dummyValue) {
		return doubles.remove(0);
	}

	@Override
	public void continueOnlyIf(boolean expression) {
		if (! expression) {
			throw new RuntimeException("I was told to only continue if this was true!");
		}
	}

}
