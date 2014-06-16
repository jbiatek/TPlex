package edu.umn.crisys.plexil.java.psx.symbolic;

public interface ValueSource {

	public int symbolicInteger(int dummyValue);
	public boolean symbolicBoolean(boolean dummyValue);
	public boolean symbolicBoolean(boolean dummyValue, double probabilityTrue);
	public double symbolicDouble(double dummyValue);
	public void continueOnlyIf(boolean expression);
	
}
