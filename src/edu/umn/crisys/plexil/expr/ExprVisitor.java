package edu.umn.crisys.plexil.expr;

import edu.umn.crisys.plexil.expr.ast.ASTLookupExpr;
import edu.umn.crisys.plexil.expr.ast.ASTOperation;
import edu.umn.crisys.plexil.expr.ast.NodeRefExpr;
import edu.umn.crisys.plexil.expr.ast.NodeTimepointExpr;
import edu.umn.crisys.plexil.expr.ast.UnresolvedVariableExpr;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

/**
 * Visitor pattern for Expressions. By default, visitor methods return the 
 * results of a more general method: for example, if you don't implement the
 * method for BooleanValue, it will instead call the method for PValue. They
 * all lead eventually to the general "Expression" method. By default, this
 * catchall method throws an exception, but you could use it to, say, 
 * propagate this visitor to arguments of expression types that you don't 
 * care about or return a default value.  
 * 
 * @author jbiatek
 *
 * @param <P>
 * @param <R>
 */
public interface ExprVisitor<P, R> {
	
	public R visit(Expression e, P param);
    
	
	//PValues
	public R visit(PValue v, P param);
	public R visit(NativeBool b, P param);
	public R visit(BooleanValue bool, P param);
	public R visit(IntegerValue integer, P param);
	public R visit(RealValue real, P param);
	public R visit(StringValue string, P param);
	public R visit(UnknownValue unk, P param);
	public R visit(PValueList<?> list, P param);
	public R visit(CommandHandleState state, P param);
	public R visit(NodeFailureType type, P param);
	public R visit(NodeOutcome outcome, P param);
	public R visit(NodeState state, P param);

	// Lookups
	public R visit(LookupExpr lookup, P param);
    public R visit(ASTLookupExpr lookup, P param);

    // AST expressions
    public R visit(ASTOperation op, P param);
    public R visit(UnresolvedVariableExpr expr, P param);
    public R visit(NodeRefExpr ref, P param);
    public R visit(NodeTimepointExpr timept, P param);

    
    // IL expressions
    public R visit(NamedCondition named, P param);
	public R visit(GetNodeStateExpr state, P param);
	public R visit(AliasExpr alias, P param);
    public R visit(RootAncestorExpr root, P param);
    public R visit(ILOperation op, P param);
    
    //ILVariables
    public R visit(ILVariable v, P param);
	public R visit(SimpleVar var, P param);
	public R visit(ArrayVar array, P param);
	public R visit(LibraryVar lib, P param);

    
}
