package edu.umn.crisys.plexil.ast.expr;

public enum NodeRefExpr implements PlexilExpr {

	PARENT,
	SIBLING,
	CHILD,
	SELF;
	
	@Override
	public <P, R> R accept(ASTExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public PlexilType getPlexilType() {
		return PlexilType.NODEREF;
	}

	@Override
	public String asString() {
		return toString().toLowerCase();
	}


}
