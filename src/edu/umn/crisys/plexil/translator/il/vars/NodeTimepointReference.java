package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.expr.common.PValueExpression;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.java.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.NodeTimepoint;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.translator.il.NodeUID;

public class NodeTimepointReference extends RHSVariable {

    private NodeUID nodePath;
    private NodeState state;
    private NodeTimepoint point;
    private boolean used = true;
    public void markAsUnused() {
        used = false;
    }
    public boolean isUsed() {
        return used;
    }
    
    public NodeTimepointReference(NodeUID nodePath, NodeState state, NodeTimepoint point) {
        this.nodePath = nodePath;
        this.state = state;
        this.point = point;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return nodePath+"."+state+"."+point;
    }
    
    public PlexilType getType() {
        return PlexilType.REAL;
    }
    
    private String getFieldName() {
        return NameUtils.clean(getName());
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        if (used) {
            JCodeModel cm = clazz.owner();
            // Get Variable<Whatever>
            JClass typeClass = cm.ref(SimpleCurrentNext.class).narrow(PNumeric.class);
    
            clazz.field(JMod.PRIVATE, typeClass, getFieldName(), 
                    JExpr._new(typeClass).arg(cm.ref(UnknownValue.class).staticInvoke("get")));
        }
    }
    
    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        if (used) {// Priority is ignored, shouldn't ever apply anyway.
            block.invoke(directReference(cm), "setNext").arg(rhs);
            block.invoke("commitAfterMicroStep").arg(directReference(cm));
        }
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm).invoke("getCurrent");
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        if (used) {
            // Looks like the exec sets the back to 0.
            addAssignment(ILExprToJava.toJava(new PValueExpression(0.0), cm), JExpr.lit(0), block, cm);
        }
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return JExpr.ref(getFieldName());
    }

    @Override
    public String getNameForTesting() {
        // Timepoints aren't even included in Plexil's logs, and it probably 
        // wouldn't work anyway.
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

