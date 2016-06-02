package edu.umn.crisys.plexil.ast.expr;

public abstract class PlexilExprBase implements PlexilExpr {

	private PlexilType type;
	
	public PlexilExprBase(PlexilType type) {
		this.type = type;
	}
	
	@Override
	public PlexilType getPlexilType() {
		return type;
	}
    
    @Override
    public final String toString() {
    	return asString();
    }
}
