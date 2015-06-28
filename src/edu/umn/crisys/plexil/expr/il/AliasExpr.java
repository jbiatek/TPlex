package edu.umn.crisys.plexil.expr.il;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.expr.PlexilType;

public class AliasExpr extends ExpressionBase {

    private String name;
    private boolean writeable;
    
    public AliasExpr(String name, PlexilType type, boolean writeable) {
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


}
