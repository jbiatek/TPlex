package edu.umn.crisys.plexil.java.values;

import edu.umn.crisys.plexil.ast.core.expr.common.CommonExprVisitor;
import edu.umn.crisys.plexil.ast.core.expr.var.ASTExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;

public class StringValue implements PString {

	private final String value;

	public static StringValue get(String init) {
		return new StringValue(init);
	}
	
	private StringValue(String initial) {
		value = initial;
	}

	/* (non-Javadoc)
	 * @see edu.umn.crisys.plexil.values.PString#getValue()
	 */
	@Override
	public String getString() {
		return value;
	}

	@Override
	public boolean isKnown() {
		return true;
	}
	
	@Override
	public boolean isUnknown() {
		return false;
	}
	
	@Override
	public PValue castTo(PlexilType type) {
		return PValue.Util.defaultCastTo(this, type);
	}
	
	@Override
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		if (!(o instanceof StringValue)) return BooleanValue.get(false);
		StringValue other = (StringValue) o;
		return BooleanValue.get(this.value.equals(other.value));
	}

	@Override
	public PlexilType getType() {
		return PlexilType.STRING;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StringValue) {
			StringValue other = (StringValue) o;
			return (this.isKnown() == other.isKnown())
					&& this.value.equals(other.value);
		} else if (o instanceof String) {
		    return this.value.equals(o);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see edu.umn.crisys.plexil.values.PString#concat(edu.umn.crisys.plexil.values.StringValue)
	 */
	@Override
	public PString concat(PString o) {
		if (o.isUnknown()) {
			return UnknownValue.get();
		}
		return StringValue.get(value + o.getString());
	}
	
	@Override
	public String toString() {
		return value;
	}

	@Override
	public <P, R> R accept(CommonExprVisitor<P, R> visitor, P param) {
		return visitor.visitStringValue(this, param);
	}

	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
		return accept((CommonExprVisitor<P, R>) visitor, param);
	}

	@Override
	public String asString() {
		return toString();
	}

	@Override
	public boolean isAssignable() {
		return false;
	}
}
