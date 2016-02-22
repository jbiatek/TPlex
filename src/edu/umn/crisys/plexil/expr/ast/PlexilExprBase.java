package edu.umn.crisys.plexil.expr.ast;

public abstract class PlexilExprBase implements PlexilExpr {

	private PlexilType type;
	
	public PlexilExprBase(PlexilType type) {
		this.type = type;
	}
	
	@Override
	public PlexilType getPlexilType() {
		return type;
	}

}
