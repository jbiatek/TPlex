package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.il.expr.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class AliasedVariableReference extends RHSVariable {

    private String name;
    
    public AliasedVariableReference(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name+" (alias)";
    }
    
    @Override
    public PlexilType getType() {
        return PlexilType.UNKNOWN;
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        // We don't add ourselves, the Plan will add a "parent" variable to the
        // class and the constructor when it sees us in its list.
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        block.add( JExpr.invoke("getInterface").invoke("performAssignment").arg(name).arg(rhs).arg(priority) );

    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return JExpr.invoke("getInterface").invoke("getValue").arg(name);
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        // No need to do anything.
    }

    @Override
    public String getNameForTesting() {
        // We're just a wrapper.
        return null;
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return null;
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
