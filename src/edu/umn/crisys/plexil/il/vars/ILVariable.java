package edu.umn.crisys.plexil.il.vars;

import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public abstract class ILVariable implements ILExpression {
	
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
		return name;
	}
	
	@Override
	public String asString() {
		return name;
	}
	
	@Override
	public final <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		if (visitor instanceof ILExprVisitor<?,?>) {
			return this.accept((ILExprVisitor<P, R>)visitor, param);
		}
		throw new RuntimeException("Visitor "+visitor+" cannot visit IL Expressions");
	}
	
	public final <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
		return this.accept((ILVarVisitor<P,R>)visitor, param);
	}

	public abstract <P, R> R accept(ILVarVisitor<P,R> visitor, P param);
	
}
