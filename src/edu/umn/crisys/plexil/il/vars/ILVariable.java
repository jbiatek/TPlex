package edu.umn.crisys.plexil.il.vars;

import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

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
	
	public final <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return this.accept((ILVarVisitor<P,R>)visitor, param);
	}

	public abstract <P, R> R accept(ILVarVisitor<P,R> visitor, P param);
	
}
