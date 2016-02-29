package edu.umn.crisys.plexil.il2java.expr;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.ASTOperation;
import edu.umn.crisys.plexil.ast.expr.ASTOperation.Operator;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.PBoolean;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILExprToJava {

	public static boolean SHORT_CIRCUITING = true;
	static final String TEMP_VAR_NAME = "temp";
    
    public static JExpression toJava(ILExpr expr, JCodeModel cm) {
        return expr.accept(new IL2Java(), cm);
    }
    
    /**
     * Without biasing, in order to implement short circuit AND and OR operations,
     * we need a temporary variable to store intermediate results in. Use this
     * method before adding a translated JExpression to insert that temporary
     * variable. 
     * @param block
     * @param cm
     */
    public static void insertShortCircuitHack(JBlock block, JCodeModel cm) {
        block.decl(cm.ref(PBoolean.class), TEMP_VAR_NAME);
    }
    
    public static boolean requiresShortCircuitHack(ILExpr expr) {
    	if (expr instanceof ASTOperation) {
    		ASTOperation op = (ASTOperation) expr;
    		if (op.getOperator() == Operator.AND || op.getOperator() == Operator.OR) {
    			return true; // these are the ones we're looking for
    		}
    	}

    	// Could be one hiding in here somewhere
    	for (ILExpr child : expr.getArguments()) {
    		if (requiresShortCircuitHack((ILExpr) child)) {
    			return true;
    		}
    	}
    	
    	// Didn't find one anywhere.
    	return false;
    }
    
    public static JExpression plexilTypeAsJava(ILType type, JCodeModel cm) {
    	return cm.ref(ILType.class).staticRef(type.toString());
    }
    
	public static String getFieldName(NodeUID nodePath, String varName) {
	    return NameUtils.clean(nodePath+"/"+varName);
	}
	
	public static String getFieldName(ILVariable v) {
		return getFieldName(v.getNodeUID(), v.getName());
	}
	
	public static String getLibraryFieldName(NodeUID libNode) {
        return "LIB_INCLUDE___"+libNode.toCleanString();
	}


    
    public static JExpression PValueToJava(PValue v, JCodeModel cm) {
        ILType type = v.getType();
        
        // Handle an UNKNOWN expression
        if (v.isUnknown()) {
            if (type.isEnumeratedType()) {
            	// These look like SomeTypeClass.UNKNOWN:
                JClass clazz = cm.ref(type.getConcreteTypeClass());
                return clazz.staticRef(type.getUnknown().toString());
            } else {
            	// Everything else uses the singleton.
                return cm.ref(UnknownValue.class).staticInvoke("get");
            }
        }
        
        // It's a known value. Is it an enum?
        if (type.isEnumeratedType()) {
        	// So we want ConcreteTypeClass.VALUE
            return cm.ref(type.getConcreteTypeClass()).staticRef(v.toString());
        }
        
        // Is it an array?
        if (type.isArrayType()) {
        	PValueList<?> array = (PValueList<?>) v;
        	ILType elements = array.getType().elementType();
        	// We need a PValueList<ElementType>.
        	Class<?> elementClass = PValue.class;
        	if (elements != ILType.UNKNOWN) {
        		elementClass = elements.getTypeClass();
        	}
        	JClass narrowed = cm.ref(PValueList.class).narrow(elementClass);
        	// All we need for the constructor are the array type, and then each 
        	// element in the array.
        	JExpression jtype = plexilTypeAsJava(array.getType(), cm);
        	JInvocation constructor = JExpr._new(narrowed);
        	constructor.arg(jtype);
        	for (PValue e : array) {
        		constructor.arg(toJava(e, cm));
        	}
        	
        	return constructor;
        }
        
        // Not unknown, an enum, or an array. Must just be a standard type.
        String nativeJava = v.toString();
        if (v.getType() == ILType.STRING) {
        	// We're dumping this directly, so it needs quotes.
            nativeJava = "\"" + ((PString)v).getString() + "\"";
        }
        return cm.ref(type.getConcreteTypeClass()).staticInvoke("get").arg(JExpr.direct(nativeJava));

    }
    
    
}
