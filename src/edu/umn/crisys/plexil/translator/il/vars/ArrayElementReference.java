package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class ArrayElementReference extends RHSVariable {

    private ArrayReference arrayRef;
    private ILExpression index;
    
    public ArrayElementReference(ArrayReference array, ILExpression index) {
        this.arrayRef = array;
        this.index = index;
        PlexilType.NUMERIC.typeCheck(index.getType());
    }

    @Override
    public String toString() {
        return arrayRef.getName() + "["+index.toString()+"]";
    }

    @Override
    public PlexilType getType() {
        return arrayRef.getType().elementType();
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        // Our array will add itself (and therefore us) to the class. Nothing to do here.
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return arrayRef.rhs(cm).invoke("get").arg(ILExprToJava.toJava(index, cm));
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        // The array should handle this. Do nothing.
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        block.add(
                arrayRef.directReference(cm).invoke("indexAssign")
                .arg(ILExprToJava.toJava(index, cm))
                .arg(rhs));
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
    	// Instead, return a direct ref to the array
        return arrayRef.directReference(cm);
    }
    

    @Override
    public String getNameForTesting() {
        // We aren't really a variable, so don't include us in testing.
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if ( ! (o instanceof ArrayElementReference)) {
            return false;
        }
        ArrayElementReference aer = (ArrayElementReference) o;
        return aer.arrayRef.equals(this.arrayRef) &&
            aer.index.equals(this.index);
    }
    
    @Override
    public int hashCode() {
        return arrayRef.hashCode() * index.hashCode();
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
