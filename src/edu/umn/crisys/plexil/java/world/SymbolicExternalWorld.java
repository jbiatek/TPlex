package edu.umn.crisys.plexil.java.world;

import java.util.ArrayList;
import java.util.List;

import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;

public class SymbolicExternalWorld implements ExternalWorld {
	
	private List<UpdateHandler> updateQueue = new ArrayList<UpdateHandler>();
	

	private boolean symbolicBoolean(boolean makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private int symbolicInt(int makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private double symbolicDouble(double makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private String symbolicString(String makeMeSymbolic) {
		return makeMeSymbolic;
	}
	
	private void psxLogEntry(int stepNum, String xml) {
		// This is just a logging method. 
	}
	
	@Override
	public void waitForNextEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stop() {
		// SPF will backtrack when depth is reached. 
		return false;
	}

	@Override
	public void update(UpdateHandler node, String key, PValue value) {
		updateQueue.add(node);
	}

	@Override
	public PValue lookupNow(PString stateName, PValue... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PValue lookupOnChange(PString stateName, PNumeric tolerance,
			PValue... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void command(CommandHandler caller, PString name, PValue... args) {
		// TODO Auto-generated method stub

	}

}
