package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.translator.il.NodeUID;

public class PreviousValueReference extends RHSVariable {

    private NodeUID nodeId;
    
    public PreviousValueReference(NodeUID node) {
        nodeId = node;
    }
    
    @Override
    public String toString() {
        return "Previous value of "+nodeId;
    }
    
    @Override
    public PlexilType getType() {
        return PlexilType.UNKNOWN;
    }

    private String getFieldName() {
        return "PREV_VALUE___"+NameUtils.clean(nodeId.toString());
    }
    
    @Override
    public void addVarToClass(JDefinedClass clazz) {
        clazz.field(JMod.PRIVATE, clazz.owner().ref(PValue.class), getFieldName());
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        throw new RuntimeException("No assigning to previous value, use me directly");
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm);
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        block.assign(JExpr.ref(getFieldName()), JExpr._null());
    }

    @Override
    public JFieldRef directReference(JCodeModel cm) {
        return JExpr.ref(getFieldName());
    }

    @Override
    public String getNameForTesting() {
        // This is not included in Plexil's logs, and it's very internal.
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
