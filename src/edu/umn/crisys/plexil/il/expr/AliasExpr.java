package edu.umn.crisys.plexil.il.expr;

public class AliasExpr extends ILExprBase {

    private String name;
    private boolean writeable;
    
    public AliasExpr(String name, ILType type, boolean writeable) {
    	super(type);
        this.name = name;
        this.writeable = writeable;
    }
    
    public String getName() {
    	return name;
    }
    
    @Override
    public String asString() {
        return name+" (alias)";
    }
    
	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean isAssignable() {
		return writeable;
	}

	@Override
	public boolean equals(ILExpr e) {
		if (this == e)
			return true;
		if (getClass() != e.getClass())
			return false;
		AliasExpr other = (AliasExpr) e;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (writeable != other.writeable)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (writeable ? 1231 : 1237);
		return result;
	}


}
