package edu.umn.crisys.plexil.il.expr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class NamedCondition extends ILExprBase {

	private NodeUID uid;
	private PlexilExprDescription description;
	private ILExpr theExpr;
	
	public NamedCondition(ILExpr e, NodeUID uid, 
			PlexilExprDescription desc) {
		super(e.getType());
		this.theExpr = e;
		this.uid = uid;
		this.description = desc;
	}
	
	public NodeUID getUid() {
		return uid;
	}

	public PlexilExprDescription getDescription() {
		return description;
	}

	public ILExpr getExpression() {
		return theExpr;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		try { 
			return visitor.visit(this, param);
		} catch (Exception e) {
			throw new RuntimeException("Exception occured while visiting "+
					getDescription()+", "+asString(), e);
		}
	}

	@Override
	public String asString() {
		return theExpr.asString();
	}

	@Override
	public List<ILExpr> getArguments() {
		return Arrays.asList(theExpr);
	}

	@Override
	public ILExpr getCloneWithArgs(List<ILExpr> args) {
		return new NamedCondition(args.get(0), uid, description);
	}

	@Override
	public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
		return theExpr.eval(mapper);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(ILExpr e) {
		if (this == e)
			return true;
		if (e == null)
			return false;
		if (getClass() != e.getClass())
			return false;
		NamedCondition other = (NamedCondition) e;
		if (description != other.description)
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

}
