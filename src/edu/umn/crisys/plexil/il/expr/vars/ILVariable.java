package edu.umn.crisys.plexil.il.expr.vars;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExpr;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(ILExpr e) {
		if (this == e)
			return true;
		if (getClass() != e.getClass())
			return false;
		ILVariable other = (ILVariable) e;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
	
}
