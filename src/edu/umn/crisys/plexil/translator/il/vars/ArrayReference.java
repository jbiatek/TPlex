package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.Expression;
import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.SimplePArray;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PValueList;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ArrayReference extends RHSVariable {

    private String arrayName;
    private NodeUID nodePath;
    private PlexilType type;
    private int maxSize;
    private PValueList<?> iv;
    
    public ArrayReference(NodeUID nodePath, String name, PlexilType type, int maxSize, PValueList<?> arr) {
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
        
        // Create JClass for SimplePArray<PBooleanOrPIntOrPWhatever>:
        JClass parameterized = cm.ref(SimplePArray.class).narrow(type.elementType().getTypeClass());
        
        // Create the initializing expression:
        // new SimplePArray<Type>(type, numElements, T... init):
        JInvocation init = JExpr._new(parameterized)
        		.arg(ILExprToJava.plexilTypeAsJava(type, cm))
        		.arg(JExpr.lit(maxSize));
        for (PValue item : iv) {
        	init.arg(ILExprToJava.toJava(item, cm));
        }

        // That's all the pieces! Let's make the field:
        //JFieldVar jvar = 
        clazz.field(JMod.PRIVATE, parameterized, getFieldName(), init);
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        block.invoke(JExpr.ref(getFieldName()), "arrayAssign").arg(rhs);
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm).invoke("getCurrent");
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        addAssignment(ILExprToJava.toJava(iv, cm), JExpr.lit(0), block, cm);
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
