package edu.umn.crisys.spf;

import java.util.LinkedList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.DFSearch;
import gov.nasa.jpf.vm.VM;

public class CheckpointSearch extends DFSearch {
	
	public static CheckpointSearch SINGLETON;
	
	private LinkedList<Integer> stack = new LinkedList<Integer>();

	public CheckpointSearch(Config config, VM vm) {
		super(config, vm);
		if (SINGLETON != null) {
			throw new RuntimeException("Two CheckpointSearches created!");
		}
		SINGLETON = this;
	}
	
	public void setCheckpoint() {
		stack.push(depth);
	}

	@Override
	public boolean backtrack() {
		/*
		boolean result = super.backtrack();
		System.out.println("New depth is "+depth);
		return result;
		/*/
		if (stack.size() == 0) { 
			return super.backtrack();
		}
		// We need to backtrack up to just before the checkpoint first.
		int backtrackTo = stack.pop() + 1;
		boolean didBacktrack = true;
		while (depth > backtrackTo && didBacktrack) {
			didBacktrack = super.backtrack();
			depth--;
			notifyStateBacktracked();
		}
		if (didBacktrack) {
			// Okay, now go up to the "intended" depth. We don't want to do
			// the depth-- or notification because our superclass will do it.
			didBacktrack = super.backtrack();
		}
		
		return didBacktrack;
		//*/
	}
	

}
