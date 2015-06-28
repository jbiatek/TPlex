package edu.umn.crisys.plexil.expr.il.vars;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.il.NodeUID;

public abstract class ILVariable implements Expression {
	
	private String name;
	private NodeUID uid;
	private PlexilType type;
	
	public ILVariable(String name, NodeUID uid, PlexilType type) {
		this.name = name;
		this.uid = uid;
		this.type = type;
	}

	public NodeUID getNodeUID() {
		return uid;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public PlexilType getType() {
		return type;
	}

	@Override
	public String toString() {
		if (name.startsWith(".")) {
			// Also include the local node name for some context
			return uid.getShortName()+name;
		}
		return name;
	}
	
	@Override
	public String asString() {
		return name;
	}
	
}
