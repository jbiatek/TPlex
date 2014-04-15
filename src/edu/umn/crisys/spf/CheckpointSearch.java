package edu.umn.crisys.spf;

import java.util.LinkedList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.DFSearch;
import gov.nasa.jpf.vm.VM;

public class CheckpointSearch extends DFSearch {
	
	private LinkedList<Integer> stack = new LinkedList<Integer>();

	protected CheckpointSearch(Config config, VM vm) {
		super(config, vm);
	}
	
	public void setCheckpoint() {
		stack.push(depth);
	}

	@Override
	public boolean backtrack() {
		int backtrackTo = stack.size() > 0 ? stack.pop() : 0;
		boolean didBacktrack = true;
		while (depth > backtrackTo && didBacktrack) {
			didBacktrack = super.backtrack();
			depth--;
		}
		return didBacktrack;
	}

}
