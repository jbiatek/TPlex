package edu.umn.crisys.plexil.runtime.values;


import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprSingletonBase;
import edu.umn.crisys.plexil.il.expr.ILType;

public class UnknownInt extends ILExprSingletonBase implements PInteger {

	public static PInteger get() {
		return SINGLETON;
	}
	private static UnknownInt SINGLETON = new UnknownInt();
	private UnknownInt() { }
	
	@Override
	public boolean isKnown() {
		return false;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}
	
	@Override
	public String asString() {
		return "UNKNOWN";
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.INTEGER;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	@Override
	public PReal castToReal() {
		return UnknownReal.get();
	}

	@Override
	public int getIntValue() {
		throw new RuntimeException("Tried to extract value from UNKNOWN");
	}

	@Override
	public PBoolean gt(PInteger o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean ge(PInteger o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean lt(PInteger o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean le(PInteger o) {
		return UnknownBool.get();
	}

	@Override
	public PInteger add(PInteger o) {
		return this;
	}

	@Override
	public PInteger sub(PInteger o) {
		return this;
	}

	@Override
	public PInteger mul(PInteger o) {
		return this;
	}

	@Override
	public PInteger div(PInteger o) {
		return this;
	}

	@Override
	public PInteger mod(PInteger o) {
		return this;
	}

	@Override
	public PInteger max(PInteger o) {
		return this;
	}

	@Override
	public PInteger min(PInteger o) {
		return this;
	}

	@Override
	public PInteger abs() {
		return this;
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
		return ILType.INTEGER;
	}

}
