package edu.umn.crisys.plexil.translator.il.vars;

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.common.ArrayLiteralExpr;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.VariableArray;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ArrayReference extends RHSVariable {

    private String arrayName;
    private NodeUID nodePath;
    private PlexilType type;
    private int maxSize;
    private ArrayLiteralExpr iv;
    
    public ArrayReference(NodeUID nodePath, String name, PlexilType type, int maxSize, ArrayLiteralExpr arr) {
        this.arrayName = name;
        this.nodePath = nodePath;
        this.type = type;
        if ( ! type.isArrayType()) {
            throw new RuntimeException("Arrays must be of type SOMETHING_ARRAY, not "+type);
        }
        this.maxSize = maxSize;
        iv = arr;
    }

    public String getName() {
        return arrayName;
    }
    
    @Override
    public String toString() {
        return arrayName + "[]";
    }

    @Override
    public PlexilType getType() {
        return type;
    }
    
    private String getFieldName() {
        return NameUtils.clean(nodePath+"/"+arrayName);
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        JCodeModel cm = clazz.owner();
        
        // Create JClass for VariableArray<PBooleanOrPIntOrPWhatever>:
        JClass parameterized = cm.ref(VariableArray.class).narrow(type.elementType().getTypeClass());
        
        // Grab the initial values
        Object[] initials = new Object[] { };
        if (iv != null) {
            List<?> values = iv.getArguments();
            initials = values.toArray();
        }

        // Create the initializing expression:
        // new PlexilArray<Type>(name, numElements, type, T... initialValues):
        JInvocation init = ILExprToJava.newArrayExpression(arrayName, type, maxSize, cm, initials);

        // That's all the pieces! Let's make the field:
        //JFieldVar jvar = 
        clazz.field(JMod.PRIVATE, parameterized, getFieldName(), init);
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        block.invoke(JExpr.ref(getFieldName()), "assignArray").arg(rhs).arg(priority);
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm);
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        block.invoke(directReference(cm), "reset");
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return JExpr.ref(getFieldName());
    }
    

    @Override
    public String getNameForTesting() {
        return arrayName;
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
