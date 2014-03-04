package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.java.plx.UpdateHandle;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class UpdateHandleReference extends RHSVariable {

    private NodeUID updateNodeId;

    public UpdateHandleReference(NodeUID node) {
        this.updateNodeId = node;
    }
    
    @Override
    public String toString() {
        return updateNodeId+".update_complete";
    }

    @Override
    public PlexilType getType() {
        return PlexilType.BOOLEAN;
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        clazz.field(JMod.PRIVATE, UpdateHandle.class, NameUtils.clean(updateNodeId+".update_handle"))
        .init(JExpr._new(clazz.owner().ref(UpdateHandle.class)).arg(JExpr.lit(updateNodeId.getShortName())));
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        throw new RuntimeException("Update handles cannot be assigned to");
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return cm.ref(BooleanValue.class).staticInvoke("get").arg(
                directReference(cm).invoke("isUpdateComplete"));
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        block.invoke(directReference(cm), "reset");        
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        return JExpr.ref(NameUtils.clean(updateNodeId+".update_handle"));
    }
    
    @Override
    public String getNameForTesting() {
        // Not included in Plexil logs
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
