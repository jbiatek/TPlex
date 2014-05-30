package edu.umn.crisys.plexil.java.world;

public interface ValueSource {

	public int symbolicInteger(int dummyValue);
	public boolean symbolicBoolean(boolean dummyValue);
	public double symbolicDouble(double dummyValue);
	public void continueOnlyIf(boolean expression);
	
}
