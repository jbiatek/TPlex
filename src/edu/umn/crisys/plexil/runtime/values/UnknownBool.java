package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILType;

public class UnknownBool implements PBoolean {

	public static UnknownBool get() {
		return SINGLETON;
	}
	private static UnknownBool SINGLETON = new UnknownBool();
	private UnknownBool() {}

	@Override
	public String toString() {
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
	public PBoolean equalTo(PValue o) {
		return this;
	}

	@Override
	public ILType getType() {
		return ILType.BOOLEAN;
	}

	@Override
	public PValue castTo(ILType type) {
		return type.getUnknown();
	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public boolean isFalse() {
		return false;
	}

	@Override
    public boolean isNotFalse() {
        return true;
    }

    @Override
    public boolean isNotTrue() {
        return true;
    }

    @Override
	public PBoolean or(PBoolean o) {
		return o.isTrue() ? o : this;
	}

	@Override
	public PBoolean xor(PBoolean o) {
		return this;
	}

	@Override
	public PBoolean and(PBoolean o) {
		return o.isFalse()? o : this;
	}

	@Override
	public PBoolean not() {
		return this;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return toString();
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.BOOLEAN;
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> v, P param) {
		return v.visit(this, param);
	}

}
