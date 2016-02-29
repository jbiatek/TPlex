package edu.umn.crisys.plexil.il.expr.vars;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExprBase;
import edu.umn.crisys.plexil.il.expr.ILType;

public abstract class ILVariable extends ILExprBase {
	
	private String name;
	private NodeUID uid;
	
	public ILVariable(String name, NodeUID uid, ILType type) {
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
