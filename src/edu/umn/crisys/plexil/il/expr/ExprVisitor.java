package edu.umn.crisys.plexil.il.expr;

import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.expr.vars.LibraryVar;
import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
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
import edu.umn.crisys.plexil.runtime.values.UnknownBool;
import edu.umn.crisys.plexil.runtime.values.UnknownInt;
import edu.umn.crisys.plexil.runtime.values.UnknownReal;
import edu.umn.crisys.plexil.runtime.values.UnknownString;

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
	
	public R visit(ILExpr e, P param);
    
	
	//PValues
	public R visit(PValue v, P param);
	public R visit(NativeBool b, P param);
	public R visit(BooleanValue bool, P param);
	public R visit(IntegerValue integer, P param);
	public R visit(RealValue real, P param);
	public R visit(StringValue string, P param);
	public R visit(UnknownBool unk, P param);
	public R visit(UnknownInt unk, P param);
	public R visit(UnknownReal unk, P param);
	public R visit(UnknownString unk, P param);
	public R visit(PValueList<?> list, P param);
	public R visit(CommandHandleState state, P param);
	public R visit(NodeFailureType type, P param);
	public R visit(NodeOutcome outcome, P param);
	public R visit(NodeState state, P param);

    // IL expressions
    public R visit(NamedCondition named, P param);
	public R visit(GetNodeStateExpr state, P param);
	public R visit(AliasExpr alias, P param);
    public R visit(RootAncestorExpr root, P param);
    public R visit(ILOperation op, P param);
    // Lookups
    public R visit(LookupExpr lookup, P param);
    
    //ILVariables
    public R visit(ILVariable v, P param);
	public R visit(SimpleVar var, P param);
	public R visit(ArrayVar array, P param);
	public R visit(LibraryVar lib, P param);

    
}
