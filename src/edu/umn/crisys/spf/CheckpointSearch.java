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
		System.out.println("Set a checkpoint at depth "+depth);
	}

	@Override
	public boolean backtrack() {
		if (stack.size() == 0) { 
			return super.backtrack();
		}
		int backtrackTo = stack.pop();
		boolean didBacktrack = true;
		while (depth > backtrackTo && didBacktrack) {
			didBacktrack = super.backtrack();
			depth--;
		}
		System.out.println("Backtracked to depth "+depth);
		return didBacktrack;
	}
	

}
