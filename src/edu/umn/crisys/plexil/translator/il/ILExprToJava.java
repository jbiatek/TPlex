package edu.umn.crisys.plexil.translator.il;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JOp;

import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayIndexExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.NodeTimepointExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.plx.VariableArray;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PString;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.StandardValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.translator.il.expr.RootAncestorEndExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootAncestorExitExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootAncestorInvariantExpr;
import edu.umn.crisys.plexil.translator.il.expr.RootParentStateExpr;
import edu.umn.crisys.plexil.translator.il.vars.ArrayElementReference;
import edu.umn.crisys.plexil.translator.il.vars.ArrayReference;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public class ILExprToJava {

    public static boolean isConstant(ILExpression ilExpr) {
        return eval(ilExpr).isKnown();
    }
    
    public static PValue eval(ILExpression ilExpr) {
        return ilExpr.accept(new ILEval(), null);
    }
    
    private static class ILEval implements ILExprVisitor<Void, PValue> {

		@Override
		public PValue visitArrayIndex(ArrayIndexExpr array, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitArrayLiteral(ArrayLiteralExpr array, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitLookupNow(LookupNowExpr lookup, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitLookupOnChange(LookupOnChangeExpr lookup, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitNodeTimepoint(NodeTimepointExpr timept, Void param) {
			return UnknownValue.get();
		}

		
		@Override
		public PValue visitOperation(Operation op, Void param) {
			List<PValue> values = new ArrayList<PValue>();
			for (Expression arg : op.getArguments()) {
				values.add(arg.accept(this, null));
			}
			return op.getOperator().eval(values);
		}

		@Override
		public PValue visitPValue(PValueExpression value, Void param) {
			return value.getValue();
		}

		@Override
		public PValue visitRootParentState(RootParentStateExpr state, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitRootParentExit(RootAncestorExitExpr ancExit,
				Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitRootParentEnd(RootAncestorEndExpr ancEnd, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitRootParentInvariant(
				RootAncestorInvariantExpr ancInv, Void param) {
			return UnknownValue.get();
		}

		@Override
		public PValue visitVariable(IntermediateVariable var, Void param) {
			return UnknownValue.get();
		}
    	
    }
    
    public static JExpression toJava(ILExpression expr, JCodeModel cm) {
        if (expr == null) {
            throw new NullPointerException();
        }
        return expr.accept(new IL2Java(), cm);
    }
    
    public static JExpression toJavaBiased(ILExpression expr, JCodeModel cm, boolean isThis) {
        // TODO: Push biasing to the leaves, like you're supposed to.
        if (isThis) {
            return toJava(expr, cm).invoke("isTrue");
        } else {
            return toJava(expr, cm).invoke("isFalse");
        }
    }
    
    private static class IL2Java implements ILExprVisitor<JCodeModel, JExpression> {
        
        @Override
        public JExpression visitArrayIndex(ArrayIndexExpr array,
                JCodeModel cm) {
            // For now, the ArrayElementRef class knows how to do this, not us.
            ArrayElementReference elem = new ArrayElementReference(
                    (ArrayReference) array.getArray(), 
                    (ILExpression) array.getIndex());
            return elem.rhs(cm);
        }

        @Override
        public JExpression visitArrayLiteral(ArrayLiteralExpr array,
                JCodeModel cm) {
        	// This should only be used by PlexilScript.
        	return newArrayExpression("anon", array.getType(), array.getArguments().size(), cm, array.getArguments().toArray());
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
        public JExpression visitNodeTimepoint(NodeTimepointExpr timept,
                JCodeModel cm) {
            return JExpr.direct("/* TODO: Node timepoint here */ null");
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
        public JExpression visitPValue(PValueExpression value, JCodeModel cm) {
            return PValueToJava(value.getValue(), cm);
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
        public JExpression visitVariable(IntermediateVariable var, JCodeModel cm) {
            return var.rhs(cm);
        }
        
    }
    
    /**
     * Create an expression for making a new VariableArray. This will return
     * <pre>new VariableArray&lt;Type&gt;(name, maxSize, type, Type... inits)</pre>
     * @param name The name of the array
     * @param type The ARRAY type, e.g. BOOLEAN_ARRAY
     * @param maxSize The max size of the array
     * @param cm
     * @param inits Anything to represent the initial values. The toString() method
     * will be called on them, and this will be used directly as the argument to
     * "IntegerValue.get()" or whatever the correct class is. For StringValue,
     * they will be wrapped in quotes.
     * @return
     */
    public static JInvocation newArrayExpression(String name, PlexilType type, int maxSize, JCodeModel cm, Object...inits) {
        if ( ! type.isArrayType()) {
            throw new RuntimeException(type+" is not an array type");
        }
        
        String elementTypeName = type.elementType().toString();
        Class<? extends PValue> elementClass = type.elementType().getConcreteTypeClass();

        
        // Create JClass for VariableArray<PBooleanOrPIntOrPWhatever>:
        JClass parameterized = cm.ref(VariableArray.class).narrow(type.elementType().getTypeClass());

        
        JInvocation init = JExpr._new(parameterized)
            .arg(JExpr.lit(name))
            .arg(JExpr.lit(maxSize))
            .arg(cm.ref(PlexilType.class).staticRef(elementTypeName));
        // And the initial values:
        for (Object o : inits) {
            init.arg(cm.ref(elementClass).staticInvoke("get")
                    .arg(JExpr.direct(o.toString())));
        }
        
        return init;
    }
    
    
    public static JExpression PValueToJava(PValue v, JCodeModel cm) {
        PlexilType type = v.getType();
        
        // Handle an UNKNOWN expression
        if (v.isUnknown()) {
            if (type.isEnumeratedType()) {
                JClass clazz = cm.ref(type.getConcreteTypeClass());
                return clazz.staticRef(type.getUnknown().toString());
            } else {
                return cm.ref(UnknownValue.class).staticInvoke("get");
            }
        }
        
        // It's a known value. Is it an enum?
        if (type.isEnumeratedType()) {
            return cm.ref(type.getConcreteTypeClass()).staticRef(v.toString());
        }
        
        // Is it an array?
        if (type.isArrayType()) {
            // Time to build up an array expression.
            @SuppressWarnings("unchecked")
            VariableArray<? extends StandardValue> arr = (VariableArray<? extends StandardValue>) v;
            
            // Let's get the invocation, but do our own initial values since newArrayExpression
            // expects Java natives, not the PValues that we have in this array.
            JInvocation arrayExpr = newArrayExpression(arr.getName(), type, arr.size(), cm);
            for (int i = 0; i < arr.size(); i++) {
                arrayExpr.arg(PValueToJava(arr.get(IntegerValue.get(i)).getValue(), cm));
            }
            return arrayExpr;
        }
        
        // Not unknown, an enum, or an array. Must just be a standard type.
        Object native_ = ((StandardValue) v).asNativeJava();
        if (native_ instanceof String) {
            native_ = "\"" + native_ + "\"";
        }
        return cm.ref(type.getConcreteTypeClass()).staticInvoke("get").arg(JExpr.direct(native_.toString()));

    }
    
    
}
