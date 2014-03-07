package edu.umn.crisys.plexil.translator.il.vars;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.ast.core.visitor.ILExprVisitor;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il2java.StateMachineToJava;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class NodeStateReference extends RHSVariable {

    private NodeUID nodeUniquePath;
    
    public NodeStateReference(NodeUID nodeId) {
        this.nodeUniquePath = nodeId;
    }

    @Override
    public String toString() {
        return nodeUniquePath+".state";
    }

    @Override
    public PlexilType getType() {
        return PlexilType.STATE;
    }

    @Override
    public void addVarToClass(JDefinedClass clazz) {
        // The node state machine will do this.
    }

    @Override
    public void addAssignment(JExpression rhs, JExpression priority, JBlock block,
            JCodeModel cm) {
        throw new RuntimeException("State variables are not assignable");
    }

    @Override
    public JExpression rhs(JCodeModel cm) {
        return JExpr.invoke(StateMachineToJava.getMappingMethodName(nodeUniquePath));
    }

    @Override
    public void reset(JBlock block, JCodeModel cm) {
        // Nothing needs to be done, the transition will move it to INACTIVE.
    }

    @Override
    public JExpression directReference(JCodeModel cm) {
        throw new RuntimeException("No direct references to state variables");
    }


    @Override
    public String getNameForTesting() {
        return nodeUniquePath.getShortName()+".state";
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
