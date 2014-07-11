package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class AliasExpr extends ILExprBase {

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
	public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
		return visitor.visitAlias(this, param);
	}

	@Override
	public boolean isAssignable() {
		return writeable;
	}


}
