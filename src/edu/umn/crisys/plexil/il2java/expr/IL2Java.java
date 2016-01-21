package edu.umn.crisys.plexil.il2java.expr;

import java.util.List;
import java.util.stream.Collectors;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JOp;

import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.il.AliasExpr;
import edu.umn.crisys.plexil.expr.il.GetNodeStateExpr;
import edu.umn.crisys.plexil.expr.il.ILExprVisitor;
import edu.umn.crisys.plexil.expr.il.ILOperation;
import edu.umn.crisys.plexil.expr.il.RootAncestorExpr;
import edu.umn.crisys.plexil.expr.il.vars.ArrayVar;
import edu.umn.crisys.plexil.expr.il.vars.LibraryVar;
import edu.umn.crisys.plexil.expr.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.JParens;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NativeBool;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PInteger;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

class IL2Java extends ILExprVisitor<JCodeModel, JExpression> {
	
    @Override
	public JExpression visit(NativeBool b, JCodeModel cm) {
		return b.getValue() ? JExpr.TRUE : JExpr.FALSE;
	}

	@Override
	public JExpression visit(ILOperation op, JCodeModel cm) {
		List<JExpression> children = op.getArguments().stream()
				.map(arg -> arg.accept(this, cm))
				.collect(Collectors.toList());
		
		switch (op.getOperator()) {
		case PBOOL_EQ:
		case PINT_EQ:
		case PREAL_EQ:
		case PSTRING_EQ:
		case PSTATE_EQ:
		case POUTCOME_EQ:
		case PFAILURE_EQ:
		case PHANDLE_EQ:
            return binaryOperation(children, "equalTo");
		case PAND:
        	if (ILExprToJava.SHORT_CIRCUITING) {
        		return shortCircuitAnd(children, cm);
        	} else {
        		return multiOperation(children, "and");
        	}
		case POR:
        	if (ILExprToJava.SHORT_CIRCUITING) {
        		return shortCircuitOr(children, cm);
        	} else {
        		return multiOperation(children, "or");
        	}
		case PXOR:
            return multiOperation(children, "xor");
		case PNOT:
            return unaryOperation(children, "not");
		case PINT_GE:
		case PREAL_GE:
            return binaryOperation(children, "ge");
		case PINT_GT:
		case PREAL_GT:
            return binaryOperation(children, "gt");
		case PINT_LE:
		case PREAL_LE:
            return binaryOperation(children, "le");
		case PINT_LT:
		case PREAL_LT:
            return binaryOperation(children, "lt");
		case PINT_ABS:
		case PREAL_ABS:
            return unaryOperation(children, "abs");
		case PINT_ADD:
		case PREAL_ADD:
            return multiOperation(children, "add");
		case PINT_DIV:
		case PREAL_DIV:
            return binaryOperation(children, "div");
		case PINT_MAX:
		case PREAL_MAX:
            return binaryOperation(children, "max");
		case PINT_MIN:
		case PREAL_MIN:
            return binaryOperation(children, "min");
		case PINT_MOD:
		case PREAL_MOD:
            return binaryOperation(children, "mod");
		case PINT_MUL:
		case PREAL_MUL:
            return binaryOperation(children, "mul");
		case PINT_SUB:
		case PREAL_SUB:
            return binaryOperation(children, "sub");
		case PREAL_SQRT:
            return unaryOperation(children, "sqrt");

		case PREAL_TO_PINT:
			return unaryOperation(children, "castToInteger");
		case TO_PREAL:
			return unaryOperation(children, "castToReal");
			
		case PSTR_CONCAT: 
			return binaryOperation(children, "concat");
		case PBOOL_INDEX:
		case PINT_INDEX:
		case PREAL_INDEX:
		case PSTRING_INDEX:
	    	return binaryOperation(children, "get");
		case ISKNOWN_OPERATOR:
			return unaryOperation(children, "isKnown");
		case CAST_PBOOL:
            return JExpr.cast(cm.ref(PBoolean.class), children.get(0));
        case CAST_PINT:
        	return JExpr.cast(cm.ref(PInteger.class), children.get(0));
        case CAST_PREAL:
        	return JExpr.cast(cm.ref(PReal.class), children.get(0));
        case CAST_PSTRING:
            return JExpr.cast(cm.ref(PString.class), children.get(0));

		case NOT :
			return children.get(0).not();
		case AND:
			return children.stream()
					.reduce(JExpression::cand)
					.orElseThrow(() -> new RuntimeException("No args???"));
		case OR:
			return children.stream()
					.reduce(JExpression::cor)
					.orElseThrow(() -> new RuntimeException("No args to operator"));
		case EQ:
			return children.get(0).eq(children.get(1));
		case DIRECT_COMPARE:
			return binaryOperation(children, "equals");
		
		case IS_TRUE:
			return unaryOperation(children, "isTrue");
		case IS_FALSE:
			return unaryOperation(children, "isFalse");
		case IS_NOT_TRUE:
			return unaryOperation(children, "isNotTrue");
		case IS_NOT_FALSE:
			return unaryOperation(children, "isNotFalse");
		case IS_KNOWN:
			return unaryOperation(children, "isKnown");
		case IS_UNKNOWN:
			return unaryOperation(children, "isUnknown");
		case WRAP_BOOLEAN:
            return cm.ref(BooleanValue.class).staticInvoke("get").arg(
                    children.get(0));
        default:
            throw new RuntimeException("Missing operation: "+op.getOperator());
		}
	}

	@Override
    public JExpression visit(LookupExpr lookup, JCodeModel cm) {
		// Whether it's a LookupNow or LookupOnChange depends on if it has a 
		// tolerance.
		JInvocation jlookup; 
		if (lookup.getTolerance().isPresent()) {
			jlookup = JExpr.invoke("getWorld").invoke("lookupOnChange")
		                .arg(lookup.getLookupName().accept(this, cm))
		                .arg(lookup.getTolerance().get().accept(this, cm));
		} else {
			jlookup = JExpr.invoke("getWorld").invoke("lookupNow")
                    .arg(lookup.getLookupName().accept(this, cm));
		}
        
        for (Expression arg : lookup.getLookupArgs()) {
            jlookup.arg(arg.accept(this, cm));
        }
        // If it has a type, it needs to be cast to that type, because the
        // external world will just return a PValue. 
        if (lookup.getType().isSpecificType()) {
        	return JExpr.cast(cm.ref(lookup.getType().getTypeClass()), jlookup);
        } else {
        	return jlookup;
        }
    }

    private JExpression unaryOperation(List<JExpression> children, String invoke) {
        if (children.size() != 1) {
            throw new RuntimeException("Expected 1 argument here");
        }
        return children.get(0).invoke(invoke);
    }
    
    private JExpression binaryOperation(List<JExpression> children, String invoke) {
        if (children.size() != 2 ) {
            throw new RuntimeException("Expected 2 arguments here");
        }
        return children.get(0).invoke(invoke).arg(children.get(1));
    }

    private JExpression multiOperation(List<JExpression> children, String invoke) {
        JExpression ret = children.get(0);
        for (int i=1; i<children.size(); i++) {
            ret = ret.invoke(invoke).arg(children.get(i));
        }
        return ret;
    }
    
    private JExpression shortCircuitAnd(List<JExpression> children, JCodeModel cm) {
        if (children.size() == 1) {
            return children.get(0);
        }
        
        // We're using ?: (conditional operator) to implement short-circuiting
        // like this:
        // BooleanValue temp;
        // result = (temp = one).isFalse() ? new BooleanValue(false) : 
        //              temp.and((temp = two).isFalse() ? new BooleanValue(false) :
        //              temp.and((temp = three).isFalse() ? new BooleanValue(false) :
        //              temp.and(four)));
        
        // We'll build from the bottom up, last to first so that the first one is
        // on the outside to be checked first.
        
        JExpression ret = children.get(children.size()-1);
        for (int i=children.size()-2; i>=0; i--) {
            // It's a standard Java "condition ? ifTrue : ifFalse" operator
            ret = JOp.cond(
                    // If args[i].isFalse(),
                    new JParens(JExpr.assign(JExpr.ref(ILExprToJava.TEMP_VAR_NAME), children.get(i))).invoke("isFalse"),
                    // return false,
                    cm.ref(BooleanValue.class).staticInvoke("get").arg(JExpr.lit(false)),
                    // else, return the result of the rest of the list.
                    // (remember, we're moving backwards)
                    JExpr.ref(ILExprToJava.TEMP_VAR_NAME).invoke("and").arg(ret));
        }
        return ret;
    }
    
    public JExpression shortCircuitOr(List<JExpression> children, JCodeModel cm) {
        if (children.size() == 1) {
            return children.get(0);
        }
        
        // We're using ?: (conditional operator) to implement short-circuiting
        // like this:
        // BooleanValue temp;
        // result = ((temp = one).isTrue() ? new BooleanValue(true) : 
        //              temp.or((temp = two).isTrue() ? new BooleanValue(true) :
        //              temp.or((temp = three).isTrue() ? new BooleanValue(true) :
        //              temp.or(four))));
        
        // We'll build from the bottom up, last to first so that the first one is
        // on the outside to be checked first.
        
        JExpression ret = children.get(children.size()-1);
        for (int i=(children.size()-2); i>=0; i--) {
            ret = JOp.cond(
                    // If args[i] is true,
                    new JParens(JExpr.assign(JExpr.ref(ILExprToJava.TEMP_VAR_NAME), children.get(i))).invoke("isTrue"),
                    // return true,
                    cm.ref(BooleanValue.class).staticInvoke("get").arg(JExpr.lit(true)),
                    // else, return the result of the rest of the list.
                    JExpr.ref(ILExprToJava.TEMP_VAR_NAME).invoke("or").arg(ret));
        }
        return ret;
    }


    @Override
    public JExpression visit(RootAncestorExpr root, JCodeModel cm) {
    	switch(root) {
    	case END:
            return JExpr.invoke("getInterface").invoke("evalAncestorEnd");
    	case EXIT:
            return JExpr.invoke("getInterface").invoke("evalAncestorExit");
    	case INVARIANT:
            return JExpr.invoke("getInterface").invoke("evalAncestorInvariant");
    	case STATE:
            return JExpr.invoke("getInterface").invoke("evalParentState");
    	default:
    		throw new RuntimeException("missing case");
    	}
    }

	@Override
	public JExpression visit(AliasExpr alias, JCodeModel param) {
        return JExpr.invoke("getInterface").invoke("getValue").arg(alias.getName());
	}

	@Override
	public JExpression visit(SimpleVar var, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getFieldName(var.getNodeUID(), var.getName())).invoke("getCurrent");
	}

	@Override
	public JExpression visit(ArrayVar array, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getFieldName(array.getNodeUID(), array.getName())).invoke("getCurrent");
	}

	@Override
	public JExpression visit(LibraryVar lib, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getLibraryFieldName(lib.getNodeUID())).invoke("getRootNodeState");
	}

	@Override
	public JExpression visit(GetNodeStateExpr state,
			JCodeModel param) {
		return JExpr.invoke(StateMachineToJava.getMappingMethodName(state.getNodeUid()));
	}

	@Override
	public JExpression visit(BooleanValue bool, JCodeModel cm) {
		return ILExprToJava.PValueToJava(bool, cm);
	}

	@Override
	public JExpression visit(IntegerValue integer,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(integer, cm);
	}

	@Override
	public JExpression visit(RealValue real, JCodeModel cm) {
		return ILExprToJava.PValueToJava(real, cm);
	}

	@Override
	public JExpression visit(StringValue string, JCodeModel cm) {
		return ILExprToJava.PValueToJava(string, cm);
	}

	@Override
	public JExpression visit(UnknownValue unk, JCodeModel cm) {
		return ILExprToJava.PValueToJava(unk, cm);
	}

	@Override
	public JExpression visit(PValueList<?> list, JCodeModel cm) {
		return ILExprToJava.PValueToJava(list, cm);
	}

	@Override
	public JExpression visit(CommandHandleState state,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(state, cm);
	}

	@Override
	public JExpression visit(NodeFailureType type,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(type, cm);
	}

	@Override
	public JExpression visit(NodeOutcome outcome,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(outcome, cm);
	}

	@Override
	public JExpression visit(NodeState state, JCodeModel cm) {
		return ILExprToJava.PValueToJava(state, cm);
	}

	@Override
	public JExpression visit(NamedCondition named, JCodeModel cm) {
		// Just inline the whole thing. 
		return named.getExpression().accept(this, cm);
	}
    
}