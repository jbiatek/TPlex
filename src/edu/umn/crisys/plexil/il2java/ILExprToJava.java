package edu.umn.crisys.plexil.il2java;

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
import edu.umn.crisys.plexil.ast.core.expr.common.Operation.Operator;
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
			// Normally, we just call variables UNKNOWN and everything works
			// out because we really don't know what they'll be. But there's
			// one exception to this...
			if (op.getOperator() == Operator.ISKNOWN) {
				PValue arg = op.getArguments().get(0).accept(this, null);
				if (arg.isUnknown()) {
					// Since Expressions can't reference UNKNOWN except through
					// variables, and we don't know if this variable will always
					// be unknown or not, the answer is actually UNKNOWN. 
					return UnknownValue.get();
				} else {
					// It must be a constant? This probably doesn't make
					// any actual sense. 
					return BooleanValue.get(true);
				}
			}
			
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
        return expr.accept(new IL2JavaBiased(isThis), cm);
    }
    
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
    private static class IL2JavaBiased extends IL2Java {
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
            	return wrap(toJava(op, cm));
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
            	if (isConstant(child)) {
            		// Can we drop this entirely?
            		PBoolean eval = (PBoolean) eval(child);
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
			return wrap(toJava(ensureBool(array), cm));
		}

		@Override
		public JExpression visitArrayLiteral(ArrayLiteralExpr array,
				JCodeModel cm) {
			throw new RuntimeException("Arrays aren't booleans.");
		}

		@Override
		public JExpression visitLookupNow(LookupNowExpr lookup, JCodeModel cm) {
			return wrap(toJava(ensureBool(lookup), cm));
		}

		@Override
		public JExpression visitLookupOnChange(LookupOnChangeExpr lookup,
				JCodeModel cm) {
			return wrap(toJava(ensureBool(lookup), cm));
		}

		@Override
		public JExpression visitNodeTimepoint(NodeTimepointExpr timept,
				JCodeModel cm) {
			throw new RuntimeException("Timepoints aren't booleans.");
		}

		@Override
		public JExpression visitPValue(PValueExpression value, JCodeModel cm) {
			return wrap(toJava(ensureBool(value), cm));
		}

		@Override
		public JExpression visitRootParentState(RootParentStateExpr state,
				JCodeModel cm) {
			return wrap(toJava(state, cm));
		}

		@Override
		public JExpression visitRootParentExit(RootAncestorExitExpr ancExit,
				JCodeModel cm) {
			return wrap(toJava(ancExit, cm));
		}

		@Override
		public JExpression visitRootParentEnd(RootAncestorEndExpr ancEnd,
				JCodeModel cm) {
			return wrap(toJava(ancEnd, cm));
		}

		@Override
		public JExpression visitRootParentInvariant(
				RootAncestorInvariantExpr ancInv, JCodeModel cm) {
			return wrap(toJava(ancInv, cm));
		}

		@Override
		public JExpression visitVariable(IntermediateVariable var,
				JCodeModel cm) {
			return wrap(toJava(ensureBool(var), cm));
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
