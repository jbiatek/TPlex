package edu.umn.crisys.plexil.il.expr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umn.crisys.plexil.runtime.values.PValue;

public class ILOperation extends ILExprBase {
	private List<ILExpr> args;
	private ILOperator op;

	public ILOperation(ILOperator op, List<ILExpr> args) {
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
		ILTypeChecker.typeCheck(this, op.getReturnType());
	}

	@Override
	public List<ILExpr> getArguments() {
		return args;
	}
	
	public ILOperator getOperator() {
		return op;
	}

	@Override
	public ILExpr getCloneWithArgs(List<ILExpr> newArgs) {
		return new ILOperation(op, newArgs);
	}
	
	@Override
	public Optional<PValue> eval(Function<ILExpr, Optional<PValue>> mapper) {
		return op.eval(args, mapper);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		return result;
	}

	@Override
	public boolean equals(ILExpr e) {
		if (this == e)
			return true;
		if (getClass() != e.getClass())
			return false;
		ILOperation other = (ILOperation) e;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (op != other.op)
			return false;
		return true;
	}
	
}
