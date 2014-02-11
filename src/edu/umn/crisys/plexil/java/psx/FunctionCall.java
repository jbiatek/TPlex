package edu.umn.crisys.plexil.java.psx;

import java.util.Arrays;

import edu.umn.crisys.plexil.java.values.PValue;

public class FunctionCall {
	
	private String name;
	private PValue[] args;
	
	public String getName() {
		return name;
	}

	public PValue[] getArgs() {
		return args;
	}

	public FunctionCall(String name, PValue... args) {
		this.name = name;
		this.args = args;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FunctionCall) {
			FunctionCall other = (FunctionCall) o;
			return (other.name.equals(name) 
					&& Arrays.equals(args, other.args));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = name.hashCode();
		for (PValue v : args) {
			hash += v.hashCode();
		}
		return hash;
	}
	
	public String toString() {
		String ret = name+"(";
		if (args.length == 0) {
			return ret + ")";
		}
		for (int i=0; i<args.length-1; i++) {
			ret += args[i]+", ";
		}
		
		return ret+args[args.length-1]+")";
	}

	
}