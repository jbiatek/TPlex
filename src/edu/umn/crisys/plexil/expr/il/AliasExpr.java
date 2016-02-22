package edu.umn.crisys.plexil.expr.il;

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


}
