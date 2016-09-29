package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public class UnknownReal implements PReal {

	public static PReal get() {
		return SINGLETON;
	}
	private static UnknownReal SINGLETON = new UnknownReal();
	private UnknownReal() {}
	
	@Override
	public boolean isKnown() {
		return false;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public ILType getType() {
		return ILType.REAL;
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
		return PlexilType.REAL;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

	@Override
	public double getRealValue() {
		throw new RuntimeException("Tried to extract value from UNKNOWN");
	}

	@Override
	public PBoolean gt(PReal o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean ge(PReal o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean lt(PReal o) {
		return UnknownBool.get();
	}

	@Override
	public PBoolean le(PReal o) {
		return UnknownBool.get();
	}

	@Override
	public PReal add(PReal o) {
		return this;
	}

	@Override
	public PReal sub(PReal o) {
		return this;
	}

	@Override
	public PReal mul(PReal o) {
		return this;
	}

	@Override
	public PReal div(PReal o) {
		return this;
	}

	@Override
	public PReal mod(PReal o) {
		return this;
	}

	@Override
	public PReal max(PReal o) {
		return this;
	}

	@Override
	public PReal min(PReal o) {
		return this;
	}

	@Override
	public PReal sqrt() {
		return this;
	}

	@Override
	public PReal abs() {
		return this;
	}

	@Override
	public PInteger ceil() {
		return UnknownInt.get();
	}

	@Override
	public PInteger floor() {
		return UnknownInt.get();
	}

	@Override
	public PInteger round() {
		return UnknownInt.get();
	}

	@Override
	public PInteger trunc() {
		return UnknownInt.get();
	}

	@Override
	public PInteger real_to_int() {
		return UnknownInt.get();
	}

}
