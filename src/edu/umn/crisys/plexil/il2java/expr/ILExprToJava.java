package edu.umn.crisys.plexil.il2java.expr;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.PlexilType;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class ILExprToJava {

	public static boolean SHORT_CIRCUITING = true;
    
    public static JExpression toJava(ILExpression expr, JCodeModel cm) {
        if (expr == null) {
            throw new NullPointerException();
        }
        return expr.accept(new IL2Java(), cm);
    }
    
    public static JExpression toJavaBiased(ILExpression expr, JCodeModel cm, boolean isThis) {
        return expr.accept(new IL2JavaBiased(isThis), cm);
    }
    
    public static JExpression plexilTypeAsJava(PlexilType type, JCodeModel cm) {
    	return cm.ref(PlexilType.class).staticRef(type.toString());
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
        PlexilType type = v.getType();
        
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
        	PlexilType elements = array.getType().elementType();
        	// We need a PValueList<ElementType>.
        	Class elementClass = PValue.class;
        	if (elements != PlexilType.UNKNOWN) {
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
        if (v.getType() == PlexilType.STRING) {
        	// We're dumping this directly, so it needs quotes.
            nativeJava = "\"" + nativeJava + "\"";
        }
        return cm.ref(type.getConcreteTypeClass()).staticInvoke("get").arg(JExpr.direct(nativeJava));

    }
    
    
}
