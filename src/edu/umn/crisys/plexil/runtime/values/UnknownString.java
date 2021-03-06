package edu.umn.crisys.plexil.runtime.values;


import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprSingletonBase;
import edu.umn.crisys.plexil.il.expr.ILType;

public class UnknownString extends ILExprSingletonBase implements PString {
	
	public static UnknownString get() {
		return SINGLETON;
	}
	private static UnknownString SINGLETON = new UnknownString();
	private UnknownString() { }

	@Override
	public String asString() {
		return "UNKNOWN";
	}
	
	@Override
	public boolean isKnown() {
		return false;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public String getString() {
		throw new RuntimeException("Tried to use the value of UNKNOWN");
	}

	@Override
	public PBoolean equalTo(PValue o) {
		return UnknownBool.get();
	}

	@Override
	public PValue castTo(ILType type) {
		return type.getUnknown();
	}

	@Override
	public PString concat(PString o) {
		return this;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.STRING;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	@Override
	public boolean equals(ILExpr e) {
		return this == e;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public ILType getType() {
		return ILType.STRING;
	}

}
