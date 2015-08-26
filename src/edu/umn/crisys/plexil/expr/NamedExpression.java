package edu.umn.crisys.plexil.expr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.PlexilExprDescription;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class NamedExpression extends ExpressionBase {

	private NodeUID uid;
	private PlexilExprDescription description;
	private Expression theExpr;
	
	public NamedExpression(Expression e, NodeUID uid, 
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

	public Expression getExpression() {
		return theExpr;
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return theExpr.asString();
	}

	@Override
	public List<Expression> getArguments() {
		return Arrays.asList(theExpr);
	}

	@Override
	public Expression getCloneWithArgs(List<Expression> args) {
		return new NamedExpression(args.get(0), uid, description);
	}

	@Override
	public Optional<PValue> eval() {
		return theExpr.eval();
	}

}
