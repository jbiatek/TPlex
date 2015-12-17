package edu.umn.crisys.plexil.expr.il;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.expr.ExprVisitor;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.ExpressionBase;
import edu.umn.crisys.plexil.runtime.values.PValue;

public class ILOperation extends ExpressionBase {
	private List<Expression> args;
	private ILOperator op;

	public ILOperation(ILOperator op, List<Expression> args) {
		super(op.getReturnType());
		this.args = Collections.unmodifiableList(args);
		this.op = op;
		if (args.isEmpty()) {
			throw new RuntimeException("Syntax error: Operator passed an "
					+ "empty list of arguments.");
		}
		if (! op.isAcceptableArgNumber(args.size())) {
			throw new RuntimeException("Syntax error: Operator "+op
					+" does not accept "+args.size()+" arguments.");
		}
		if (! op.isTypeSafe(getArguments())) {
			throw new RuntimeException("Type safety error: Operator "+op
					+" says that "+args+" is not type safe");
		}
	}

	@Override
	public List<Expression> getArguments() {
		return args;
	}
	
	public ILOperator getOperator() {
		return op;
	}

	@Override
	public Expression getCloneWithArgs(List<Expression> newArgs) {
		return new ILOperation(op, newArgs);
	}
	
	@Override
	public Optional<PValue> eval() {
		return op.eval(args);
	}

	@Override
	public boolean isAssignable() {
		return op.isAssignable();
	}

	@Override
	public <P, R> R accept(ExprVisitor<P, R> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public String asString() {
		return args.stream().map(Object::toString)
				.collect(Collectors.joining(
						op.getStringDeliminator(), 
						op.getStringPrefix(), 
						op.getStringSuffix()));
	}
	
}
