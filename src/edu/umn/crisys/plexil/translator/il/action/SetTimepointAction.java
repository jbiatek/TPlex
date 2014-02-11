package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

import edu.umn.crisys.plexil.ast.core.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.core.expr.common.Operation;
import edu.umn.crisys.plexil.translator.il.ILExprToJava;
import edu.umn.crisys.plexil.translator.il.vars.NodeTimepointReference;

public class SetTimepointAction implements PlexilAction {
    
    private NodeTimepointReference nt;

    public SetTimepointAction(NodeTimepointReference nt) {
        this.nt = nt;
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        if (nt.isUsed()) {
            nt.addAssignment(ILExprToJava.toJava(Operation.castToNumeric(new LookupNowExpr("time")), cm),
                    JExpr.lit(0), block, cm);
        }
    }

    @Override
    public String toString() {
        return "Set "+nt.toString();
    }
}
