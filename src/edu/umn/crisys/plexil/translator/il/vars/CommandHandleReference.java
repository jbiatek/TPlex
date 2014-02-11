package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.java.plx.CommandHandle;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.StandardValue;
import edu.umn.crisys.plexil.translator.il.NodeUID;

public class CommandHandleReference extends RHSVariable {

    private NodeUID nodeId;
    private IntermediateVariable assignReturnTo = null;
    private int priority = Integer.MAX_VALUE;

    public CommandHandleReference(NodeUID node, int priority) {
        this(node, priority, null);
    }
    
    public CommandHandleReference(NodeUID node, int priority, IntermediateVariable returnTo) {
        this.nodeId = node;
        this.priority = priority;
        this.assignReturnTo = returnTo;
    }

    @Override
    public String toString() {
        return nodeId+".command_handle";
    }

    @Override
    public PlexilType getType() {
        return PlexilType.COMMAND_HANDLE;
    }
    
    private String getFieldName() {
        return NameUtils.clean(nodeId+".command_handle");
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        JExpression init;
        JCodeModel cm = clazz.owner();
        if (assignReturnTo == null) {
            init = JExpr._new(clazz.owner().ref(CommandHandle.class));
        } else {
            // Override the return method to do the assignment when it comes in
            JDefinedClass anonClass = cm.anonymousClass(CommandHandle.class);
            JMethod ret = anonClass.method(JMod.PUBLIC, cm.VOID, "commandReturns");
            JVar value = ret.param(cm.ref(StandardValue.class), "value");
            
            
            assignReturnTo.addAssignment(value, JExpr.lit(priority), ret.body(), cm);
            ret.body().invoke(assignReturnTo.directReference(cm), "commit");
            
            init = JExpr._new(anonClass);
        }
        
        
        clazz.field(JMod.PUBLIC, clazz.owner().ref(CommandHandle.class), getFieldName(), init);
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        throw new RuntimeException("Command handles are not assignable within a PLEXIL plan");
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return directReference(cm).invoke("getCommandHandle");
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
        return nodeId.getShortName()+".command_handle";
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
