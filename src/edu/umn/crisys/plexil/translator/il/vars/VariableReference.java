package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.Variable;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class VariableReference extends RHSVariable {
    
	private NodeUID nodePath;
	private String varName;
	private PlexilType type;
	
	// optional: an initial value. This shouldn't be listed as unknown here,
	// because if it's uninitialized we're going to be initializing with the
	// type variable instead of an Expression.
	public PValue initialValue = null;
	
	public VariableReference(NodeUID nodePath, String varName, PlexilType type, PValue initial) {
	    this.varName = varName;
	    this.nodePath = nodePath;
	    this.type = type;
	    
	    // Initial value may exist, or not.
	    if (initial != null) {
	        initialValue = initial;
	    }
	}
	
	/**
	 * Special constructor for enumerated types. 
	 * @param nodeId
	 * @param type
	 * @param initialValue
	 */
	public VariableReference(NodeUID nodeId, PlexilType type, String initialValue) {
	    if ( ! type.isEnumeratedType() ) {
	        throw new RuntimeException("This constructor is for enumerated types only");
	    }
	    this.nodePath = nodeId;
	    this.varName = nodeId.getShortName()+"."+(type.toString().toLowerCase());
	    this.type = type;
	    this.initialValue = type.parseValue(initialValue);
	}
	
	@Override
	public String toString() {
	    return varName+ " ("+nodePath+")";
	}
	
	public String getName() {
	    return varName;
	}
	
	public PlexilType getType() {
	    return type;
	}
	
	private String getFieldName() {
	    return NameUtils.clean(nodePath+"/"+varName);
	}

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        JCodeModel cm = clazz.owner();
        // Get Variable<Whatever>
        JClass varContainerClass = cm.ref(Variable.class).narrow(type.getTypeClass());

        // Prepare the initial value, if any
        JExpression initExpr = getInitialValueArgument(cm);

        
        // Now we can create the field
        // private Variable<Whatever> varId_ = new Variable<Whatever>(varName, init);
        // JFieldVar field = 
        clazz.field(JMod.PRIVATE, varContainerClass, getFieldName(), 
                JExpr._new(varContainerClass).arg(nodePath+"/"+varName).arg(initExpr));
    }
    
    /**
     * Returns the correct thing to pass to the Variable constructor.
     * @param cm
     * @return
     */
    private JExpression getInitialValueArgument(JCodeModel cm) {
        if (initialValue != null) {
            return ILExprToJava.toJava(initialValue, cm);
        } 
        // There is no initial value. Instead, we'll supply the PlexilType
        // object, which the Variable will use to get UNKNOWN.
        return cm.ref(PlexilType.class).staticRef(type.toString());
    }


    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        block.invoke(directReference(cm), "setValue").arg(rhs).arg(priority);
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm).invoke("getValue");
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        block.invoke(directReference(cm), "reset");
    }

    @Override
    public String getNameForTesting() {
        return varName;
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return JExpr.ref(getFieldName());
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public <P, R> R accept(ILExprVisitor<P, R> visitor, P param) {
        return visitor.visitVariable(this, param);
    }

}
