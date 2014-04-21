package edu.umn.crisys.plexil.il2java.expr;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JOp;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.il.expr.AliasExpr;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.LibraryVar;
import edu.umn.crisys.plexil.il.vars.SimpleVar;
import edu.umn.crisys.plexil.il2java.JParens;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;

class IL2Java implements ILExprVisitor<JCodeModel, JExpression> {
	
    @Override
    public JExpression visitArrayIndex(ArrayIndexExpr array,
            JCodeModel cm) {
    	return array.getArray().accept(this, cm)
    			.invoke("get")
    			.arg(array.getIndex().accept(this, cm));
    }
    
    @Override
    public JExpression visitLookupNow(LookupNowExpr lookup, JCodeModel cm) {
        JInvocation jlookup = 
            JExpr.invoke("getWorld").invoke("lookupNow")
                .arg(lookup.getLookupName().accept(this, cm));
        
        for (Expression arg : lookup.getLookupArgs()) {
            jlookup.arg(arg.accept(this, cm));
        }
        return jlookup;
    }

    @Override
    public JExpression visitLookupOnChange(LookupOnChangeExpr lookup,
            JCodeModel cm) {
        JInvocation jlookup = 
            JExpr.invoke("getWorld").invoke("lookupOnChange")
                .arg(lookup.getLookupName().accept(this, cm))
                .arg(lookup.getTolerance().accept(this, cm));
        
        for (Expression arg : lookup.getLookupArgs()) {
            jlookup.arg(arg.accept(this, cm));
        }
        return jlookup;
    }

    @Override
    public JExpression visitOperation(Operation op, JCodeModel cm) {
        List<JExpression> children = new ArrayList<JExpression>();
        for (Expression child : op.getArguments()) {
            children.add(child.accept(this, cm));
        }
        
        switch (op.getOperator()) {
        case ABS:
            return unaryOperation(children, "abs");
        case ADD:
            return multiOperation(children, "add");
        case AND: 
            return shortCircuitAnd(children, cm);
        case CAST_BOOL:
            return JExpr.cast(cm.ref(PBoolean.class), children.get(0));
        case CAST_NUMERIC:
            return JExpr.cast(cm.ref(PNumeric.class), children.get(0));
        case CAST_STRING:
            return JExpr.cast(cm.ref(PString.class), children.get(0));
        case CONCAT:
            return binaryOperation(children, "concat");
        case DIV:
            return binaryOperation(children, "div");
        case EQ:
            return binaryOperation(children, "equalTo");
        case GE:
            return binaryOperation(children, "ge");
        case GET_COMMAND_HANDLE:
        case GET_FAILURE:
        case GET_OUTCOME:
        case GET_STATE:
            throw new RuntimeException("Variable should already have been resolved");
        case GT:
            return binaryOperation(children, "gt");
        case ISKNOWN:
            // isKnown() returns a Java boolean, not a PBoolean.
            // Biasing would make this not a problem.
            return cm.ref(BooleanValue.class).staticInvoke("get").arg(
                    unaryOperation(children, "isKnown"));
        case LE:
            return binaryOperation(children, "le");
        case LT:
            return binaryOperation(children, "lt");
        case MAX:
            return binaryOperation(children, "max");
        case MIN:
            return binaryOperation(children, "min");
        case MOD:
            return binaryOperation(children, "mod");
        case MUL:
            return multiOperation(children, "mul");
        case NE:
            return binaryOperation(children, "equalTo").invoke("not");
        case NOT:
            return unaryOperation(children, "not");
        case OR:
            return shortCircuitOr(children, cm);
        case SQRT:
            return unaryOperation(children, "sqrt");
        case SUB:
            return binaryOperation(children, "sub");
        case XOR:
            return binaryOperation(children, "xor");
        default:
            throw new RuntimeException("Missing operation: "+op.getOperator());
        }
    }
    
    private JExpression unaryOperation(List<JExpression> children, String invoke) {
        if (children.size() > 1) {
            throw new RuntimeException("Expected 1 argument here");
        }
        return children.get(0).invoke(invoke);
    }
    
    private JExpression binaryOperation(List<JExpression> children, String invoke) {
        if (children.size() > 2) {
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
                    new JParens(JExpr.assign(JExpr.ref("temp"), children.get(i))).invoke("isFalse"),
                    // return false,
                    cm.ref(BooleanValue.class).staticInvoke("get").arg(JExpr.lit(false)),
                    // else, return the result of the rest of the list.
                    // (remember, we're moving backwards)
                    JExpr.ref("temp").invoke("and").arg(ret));
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
                    new JParens(JExpr.assign(JExpr.ref("temp"), children.get(i))).invoke("isTrue"),
                    // return true,
                    cm.ref(BooleanValue.class).staticInvoke("get").arg(JExpr.lit(true)),
                    // else, return the result of the rest of the list.
                    JExpr.ref("temp").invoke("or").arg(ret));
        }
        return ret;
    }


    @Override
    public JExpression visitRootParentState(RootParentStateExpr state,
            JCodeModel cm) {
        return JExpr.invoke("getInterface").invoke("evalParentState");
    }

    @Override
    public JExpression visitRootParentExit(RootAncestorExitExpr ancExit,
            JCodeModel cm) {
        return JExpr.invoke("getInterface").invoke("evalAncestorExit");

    }

    @Override
    public JExpression visitRootParentEnd(RootAncestorEndExpr ancEnd,
            JCodeModel cm) {
        return JExpr.invoke("getInterface").invoke("evalAncestorEnd");
    }

    @Override
    public JExpression visitRootParentInvariant(
            RootAncestorInvariantExpr ancInv, JCodeModel cm) {
        return JExpr.invoke("getInterface").invoke("evalAncestorInvariant");
    }

	@Override
	public JExpression visitAlias(AliasExpr alias, JCodeModel param) {
        return JExpr.invoke("getInterface").invoke("getValue").arg(alias.getName());
	}

	@Override
	public JExpression visitSimple(SimpleVar var, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getFieldName(var.getNodeUID(), var.getName())).invoke("getCurrent");
	}

	@Override
	public JExpression visitArray(ArrayVar array, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getFieldName(array.getNodeUID(), array.getName())).invoke("getCurrent");
	}

	@Override
	public JExpression visitLibrary(LibraryVar lib, JCodeModel param) {
		return JExpr.ref(ILExprToJava.getLibraryFieldName(lib.getNodeUID())).invoke("getRootNodeState");
	}

	@Override
	public JExpression visitGetNodeState(GetNodeStateExpr state,
			JCodeModel param) {
		return JExpr.invoke(StateMachineToJava.getMappingMethodName(state.getNodeUid()));
	}

	@Override
	public JExpression visitBooleanValue(BooleanValue bool, JCodeModel cm) {
		return ILExprToJava.PValueToJava(bool, cm);
	}

	@Override
	public JExpression visitIntegerValue(IntegerValue integer,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(integer, cm);
	}

	@Override
	public JExpression visitRealValue(RealValue real, JCodeModel cm) {
		return ILExprToJava.PValueToJava(real, cm);
	}

	@Override
	public JExpression visitStringValue(StringValue string, JCodeModel cm) {
		return ILExprToJava.PValueToJava(string, cm);
	}

	@Override
	public JExpression visitUnknownValue(UnknownValue unk, JCodeModel cm) {
		return ILExprToJava.PValueToJava(unk, cm);
	}

	@Override
	public JExpression visitPValueList(PValueList<?> list, JCodeModel cm) {
		return ILExprToJava.PValueToJava(list, cm);
	}

	@Override
	public JExpression visitCommandHandleState(CommandHandleState state,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(state, cm);
	}

	@Override
	public JExpression visitNodeFailure(NodeFailureType type,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(type, cm);
	}

	@Override
	public JExpression visitNodeOutcome(NodeOutcome outcome,
			JCodeModel cm) {
		return ILExprToJava.PValueToJava(outcome, cm);
	}

	@Override
	public JExpression visitNodeState(NodeState state, JCodeModel cm) {
		return ILExprToJava.PValueToJava(state, cm);
	}
    
}