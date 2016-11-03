package edu.umn.crisys.plexil.runtime.values;

import edu.umn.crisys.plexil.ast.expr.ASTExprVisitor;
import edu.umn.crisys.plexil.ast.expr.PlexilType;
import edu.umn.crisys.plexil.il.expr.ExprVisitor;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprBase;
import edu.umn.crisys.plexil.il.expr.ILType;

public class StringValue extends ILExprBase implements PString {

	private final String value;

	public static StringValue get(String init) {
		return new StringValue(init);
	}
	
	private StringValue(String initial) {
		super(ILType.STRING);
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
	public PBoolean equalTo(PValue o) {
		if (o.isUnknown()) {
			return UnknownBool.get();
		}
		if (!(o instanceof StringValue)) return BooleanValue.get(false);
		StringValue other = (StringValue) o;
		return BooleanValue.get(this.value.equals(other.value));
	}

	@Override
	public boolean equals(ILExpr o) {
		if (o instanceof StringValue) {
			StringValue other = (StringValue) o;
			return (this.isKnown() == other.isKnown())
					&& this.value.equals(other.value);
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
			return UnknownString.get();
		}
		return StringValue.get(value + o.getString());
	}
	
	@Override
	public String asString() {
		return "\""+value+"\"";
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
}
