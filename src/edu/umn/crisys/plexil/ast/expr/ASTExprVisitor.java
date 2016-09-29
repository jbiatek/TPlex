package edu.umn.crisys.plexil.ast.expr;

import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownBool;
import edu.umn.crisys.plexil.runtime.values.UnknownInt;
import edu.umn.crisys.plexil.runtime.values.UnknownReal;
import edu.umn.crisys.plexil.runtime.values.UnknownString;

public abstract class ASTExprVisitor<P,R> {
	
	//PValues
	public abstract R visit(BooleanValue bool, P param);
	public abstract R visit(IntegerValue integer, P param);
	public abstract R visit(RealValue real, P param);
	public abstract R visit(StringValue string, P param);
	public abstract R visit(UnknownBool unk, P param);
	public abstract R visit(UnknownInt unk, P param);
	public abstract R visit(UnknownReal unk, P param);
	public abstract R visit(UnknownString unk, P param);
	public abstract R visit(PValueList<?> list, P param);
	public abstract R visit(CommandHandleState state, P param);
	public abstract R visit(NodeFailureType type, P param);
	public abstract R visit(NodeOutcome outcome, P param);
	public abstract R visit(NodeState state, P param);


	// AST expressions
	public abstract R visit(ASTLookupExpr lookup, P param);
	public abstract R visit(ASTOperation op, P param);
	public abstract R visit(UnresolvedVariableExpr expr, P param);
	public abstract R visit(NodeRefExpr ref, P param);
	public abstract R visit(NodeTimepointExpr timept, P param);

}
