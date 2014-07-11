package edu.umn.crisys.plexil.runtime.psx.symbolic;

public class SPFValues implements ValueSource {

	@Override
	public int symbolicInteger(int dummyValue) {
		return dummyValue;
	}

	@Override
	public boolean symbolicBoolean(boolean dummyValue, double probabilityTrue) {
		return dummyValue;
	}

	@Override
	public boolean symbolicBoolean(boolean dummyValue) {
		return dummyValue;
	}
	
	@Override
	public double symbolicDouble(double dummyValue) {
		return dummyValue;
	}


}
