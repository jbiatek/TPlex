package edu.umn.crisys.plexil.java.psx.symbolic;

import gov.nasa.jpf.vm.Verify;

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

	@Override
	public void continueOnlyIf(boolean expression) {
		Verify.ignoreIf( ! expression);
	}

}
