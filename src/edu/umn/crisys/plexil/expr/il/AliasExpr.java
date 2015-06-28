package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.PlexilType;

public class AliasExpr implements Expression {

    private String name;
    private PlexilType type;
    private boolean writeable;
    
    public AliasExpr(String name, PlexilType type, boolean writeable) {
        this.name = name;
        this.type = type;
        this.writeable = writeable;
    }
    
    public String getName() {
    	return name;
    }
    
    @Override
    public String toString() {
        return name+" (alias)";
    }
    
    @Override
    public PlexilType getType() {
        return type;
    }

    @Override
    public String asString() {
        return toString();
    }

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean isAssignable() {
		return writeable;
	}


}
