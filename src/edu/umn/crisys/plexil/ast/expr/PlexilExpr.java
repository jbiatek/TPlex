package edu.umn.crisys.plexil.ast.expr;

import java.util.Collections;
import java.util.List;

public interface PlexilExpr {

	public PlexilType getPlexilType();
	
	public String asString();
	
	public default List<PlexilExpr> getPlexilArguments() {
		return Collections.emptyList();
	}
	
    public <P,R> R accept(ASTExprVisitor<P,R> v, P param);
	
}
