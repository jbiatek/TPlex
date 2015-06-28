package edu.umn.crisys.plexil.expr.il.vars;

import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.PlexilType;
import edu.umn.crisys.plexil.il.NodeUID;

public abstract class ILVariable extends ExpressionBase {
	
	private String name;
	private NodeUID uid;
	
	public ILVariable(String name, NodeUID uid, PlexilType type) {
		super(type);
		this.name = name;
		this.uid = uid;
	}

	public NodeUID getNodeUID() {
		return uid;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String asString() {
		if (name.startsWith(".")) {
			// Also include the local node name for some context
			return uid.getShortName()+name;
		}
		return name;
	}
	
}
