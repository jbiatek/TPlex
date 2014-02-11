package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.translator.il.ILExprToJava;
import edu.umn.crisys.plexil.translator.il.vars.IntermediateVariable;

public class AssignAction implements PlexilAction {

    private IntermediateVariable lhs;
    private ILExpression rhs;
    private JExpression priority;
    
    
    public AssignAction(IntermediateVariable vr, ILExpression rhs, int priority) {
        this(vr, rhs, JExpr.lit(priority));
    }
    
    public AssignAction(IntermediateVariable vr, ILExpression rhs, JExpression priority) {
        this.lhs = vr;
        this.rhs = rhs;
        this.priority = priority;
    }
    
    public ILExpression getRHS() {
        return rhs;
    }

    @Override
    public String toString() {
        return "Assignment: "+lhs+" = "+rhs;
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        addAssignmentToBlock(block, lhs, ILExprToJava.toJava(rhs, cm), priority, cm);
    }

    public static void addAssignmentToBlock(JBlock block, IntermediateVariable lhs, 
            JExpression rhs, JExpression priority, JCodeModel cm) {
        //block.decl(cm.ref(PBoolean.class), "temp", JExpr._null());
        lhs.addAssignment(rhs, priority, block, cm);
        JExpression direct = lhs.directReference(cm);
        if (direct != null) {
            block.invoke("commitAfterMacroStep").arg(lhs.directReference(cm));
            block.invoke("endMacroStep");
        }

    }

}
