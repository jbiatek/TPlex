package edu.umn.crisys.plexil.il.expr.nativebool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NativeOperation implements NativeExpr {
	
	public static enum NativeOp {
		AND, OR, NOT
	}
	
	private NativeOp operation;
	private List<NativeExpr> args;
	
	public NativeOperation(NativeOp op, NativeExpr...args) {
		this(op, Arrays.asList(args));
	}

	public NativeOperation(NativeOp op, List<NativeExpr> args) {
		if (op == NativeOp.NOT && args.size() != 1) {
			throw new IllegalArgumentException("Not operator takes 1 arg, not "+args.size());
		} else if (args.isEmpty()) {
			throw new IllegalArgumentException("Need at least 1 argument");
		}
		this.operation = op;
		this.args = new ArrayList<>(args);
	}

	public NativeOp getOperation() {
		return operation;
	}

	/**
	 * Get the arguments to this operation. This is designed to be mutable, 
	 * unlike most other expressions in TPlex. 
	 * 
	 * @return a mutable list of the arguments to this operation. 
	 */
	public List<NativeExpr> getArgs() {
		return args;
	}
	
	public void addClause(NativeExpr newClause) {
		if (newClause == this) {
			throw new RuntimeException("Circular reference!!!!!");
		} else if (operation == NativeOp.NOT) {
			throw new RuntimeException("Can't add clauses to NOT");
		}
		args.add(newClause);
	}
	
	public void setArgs(List<NativeExpr> newArgs) {
		for (NativeExpr newArg : newArgs) {
			if (newArg == this) {
				throw new RuntimeException("Circular reference!!!!!");
			}
		}
		this.args = new ArrayList<>(newArgs);
	}

	@Override
	public <P, R> R accept(NativeExprVisitor<P, R> visitor, P param) {
		return visitor.visitNativeOperation(this, param);
	}
	
	@Override
	public String toString() {
		if (operation == NativeOp.NOT) {
			return "not ("+args.get(0)+")";
		}
		String operator = operation == NativeOp.AND ? " and " : " or ";
		return args.stream()
				.map((arg) -> "("+arg+")")
				.collect(Collectors.joining(operator));
	}
	
}
