package edu.umn.crisys.plexil.il2java.expr;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation.Operator;
import edu.umn.crisys.plexil.il.expr.ILEval;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

/**
 * Translate a boolean Expression with biasing. If you pass this to 
 * something that doesn't return a PBoolean, it almost certainly won't work.
 * 
 * <p>Construct it with a biasing direction, isThis. The expression
 * returned is equivalent to "(whateverPlexilExpression).isThis()", where
 * "isThis" is either "isTrue" or "isFalse" depending on which you pass in.
 * So if you're going to call "isTrue" or "isFalse" on the resulting 
 * expression anyway, use this. 
 * 
 * <p>Behind the scenes, the biasing direction will be passed down as far as
 * possible, allowing this translator to use Java native operations instead
 * of the Plexil ones. So, for example, instead of 
 * "(temp=foo).isFalse()?BooleanValue.get(false):temp.and(bar)", 
 * you'll instead get "foo.isTrue() && bar.isTrue()". Which includes the
 * same short circuiting behavior, but is much nicer and faster. 
 * 
 * <p>For good measure, it also removes redundant clauses from AND and OR
 * expressions. For example, it's very easy to end up with lots of "true &&
 * true &&...." or "false || false || ..." because of all the automatic
 * conditions. 
 * 
 * @author jbiatek
 *
 */
class IL2JavaBiased implements ILExprVisitor<JCodeModel, JExpression> {
	private boolean isThis;
	public IL2JavaBiased(boolean isThis) {
		this.isThis = isThis;
	}
	
	private ILExpression ensureBool(ILExpression e) {
		if (e.getType() != PlexilType.BOOLEAN) {
			return Operation.castToBoolean(e);
		}
		return e;
	}
	
	private JExpression wrap(JExpression jexpr){
		if (isThis) {
			return jexpr.invoke("isTrue");
		} else {
			return jexpr.invoke("isFalse");
		}
	}
	
    @Override
    public JExpression visitOperation(Operation op, JCodeModel cm) {
        switch (op.getOperator()) {
        case AND: 
        case OR:
        	List<ILExpression> ilChildren = getChildren(op);
        	List<JExpression> jArgs = translate(ilChildren, cm);
        	
        	// Now we have a bunch of boolean expressions ending in isThis().
        	if (op.getOperator() == Operator.AND) {
        		if (isThis) {
        			// If we just want to know if this isTrue, it's easy.
        			return foldWithAnd(jArgs);
        		} else {
        			// This is a little counterintuitive. We have a bunch of
        			// isFalse() clauses, and we want to know if the whole 
        			// statement isFalse(). If any of these is false, then the
        			// AND statement is, in fact, false, so we actually
        			// want to return true. 
        			return foldWithOr(jArgs);
        		}
        	} else {
        		if (isThis) {
        			return foldWithOr(jArgs);
        		} else {
        			// This is similarly counterintuitive, but right for the
        			// same reasons. 
        			return foldWithAnd(jArgs);
        		}
        	}
        case NOT:
        	// All we need to do is flip our bias around. 
        	return op.getArguments().get(0).accept(new IL2JavaBiased( ! isThis), cm);
        default:
        	// For us, this is the end of the line. We will wrap whatever
        	// the default one returns.
        	return wrap(ILExprToJava.toJava(op, cm));
        }
    }

    /**
     * Prune redundant children, and cast them all to ILExpressions. All of
     * them are expected to be PBooleans themselves. 
     * @param op
     * @return
     */
    private List<ILExpression> getChildren(Operation op) {
    	List<ILExpression> children = new ArrayList<ILExpression>();
        for (Expression childExpr : op.getArguments()) {
        	ILExpression child = (ILExpression) childExpr;
        	if (ILEval.isConstant(child)) {
        		// Can we drop this entirely?
        		PBoolean eval = (PBoolean) ILEval.eval(child);
        		if (op.getOperator() == Operator.AND && eval.isTrue()) {
        			continue;
        		} else if (op.getOperator() == Operator.OR && eval.isFalse()) {
        			continue;
        		}
        	}
        	children.add(child);
        }
    	return children;
    }
    /**
     * Translate using the same biasing direction. All of these Expressions
     * had better be PBooleans.
     * 
     * @param children
     * @param cm
     * @return
     */
    private List<JExpression> translate(List<ILExpression> children, JCodeModel cm) {
    	List<JExpression> jexpr = new ArrayList<JExpression>();
    	for (ILExpression child : children) {
    		jexpr.add(child.accept(this, cm));
    	}
    	return jexpr;
    }
    
    private JExpression foldWithAnd(List<JExpression> args) {
    	return foldWithSomething(args, true);
    }
    
    private JExpression foldWithOr(List<JExpression> args) {
    	return foldWithSomething(args, false);
    }
    
    private JExpression foldWithSomething(List<JExpression> args, boolean useAnd) {
    	JExpression ret = args.get(0);
    	for (int i=1; i < args.size(); i++) {
    		if (useAnd) {
    			ret = ret.cand(args.get(i));
    		} else {
    			ret = ret.cor(args.get(i));
    		}
    	}
    	return ret;
    }
    
	@Override
	public JExpression visitArrayIndex(ArrayIndexExpr array,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ensureBool(array), cm));
	}

	@Override
	public JExpression visitPValueList(PValueList<? >array,
			JCodeModel cm) {
		throw new RuntimeException("Arrays aren't booleans.");
	}

	@Override
	public JExpression visitLookupNow(LookupNowExpr lookup, JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ensureBool(lookup), cm));
	}

	@Override
	public JExpression visitLookupOnChange(LookupOnChangeExpr lookup,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ensureBool(lookup), cm));
	}

	@Override
	public JExpression visitNodeTimepoint(NodeTimepointExpr timept,
			JCodeModel cm) {
		throw new RuntimeException("Timepoints aren't booleans.");
	}

	@Override
	public JExpression visitBooleanValue(BooleanValue value, JCodeModel cm) {
		// These should probably be filtered out by an optimization. 
		return wrap(ILExprToJava.toJava(value, cm));
	}
	
	@Override
	public JExpression visitUnknownValue(UnknownValue value, JCodeModel cm) {
		// These should probably be filtered out by an optimization. 
		return wrap(ILExprToJava.toJava(value, cm));
	}
	
	@Override
	public JExpression visitRootParentState(RootParentStateExpr state,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(state, cm));
	}

	@Override
	public JExpression visitRootParentExit(RootAncestorExitExpr ancExit,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ancExit, cm));
	}

	@Override
	public JExpression visitRootParentEnd(RootAncestorEndExpr ancEnd,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ancEnd, cm));
	}

	@Override
	public JExpression visitRootParentInvariant(
			RootAncestorInvariantExpr ancInv, JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ancInv, cm));
	}

	@Override
	public JExpression visitVariable(IntermediateVariable var,
			JCodeModel cm) {
		return wrap(ILExprToJava.toJava(ensureBool(var), cm));
	}

	@Override
	public JExpression visitIntegerValue(IntegerValue integer, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+integer);
	}

	@Override
	public JExpression visitRealValue(RealValue real, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+real);
	}

	@Override
	public JExpression visitStringValue(StringValue string, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+string);
	}

	@Override
	public JExpression visitCommandHandleState(CommandHandleState state,
			JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+state);
	}

	@Override
	public JExpression visitNodeFailure(NodeFailureType type, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+type);
	}

	@Override
	public JExpression visitNodeOutcome(NodeOutcome outcome, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+outcome);
	}

	@Override
	public JExpression visitNodeState(NodeState state, JCodeModel param) {
		throw new RuntimeException("Not a boolean: "+state);
	}
}