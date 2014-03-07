package edu.umn.crisys.plexil.psx2java;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il2java.ILExprToJava;
import edu.umn.crisys.plexil.java.values.PValue;

public class StateEvent implements ScriptEvent {

    private String name;
    private List<PValue> params = new ArrayList<PValue>();
    private ILExpression returnValue;
    
    public StateEvent(String name, List<PValue> params, ILExpression returnValue) {
        this.name = name;
        this.params = params;
        this.returnValue = returnValue;
    }
    
    @Override
    public JExpression toJava(JCodeModel cm) {
        JInvocation invoke = 
            JExpr.invoke("stateChange")
                .arg(ILExprToJava.toJava(returnValue, cm))
                .arg(JExpr.lit(name));
        for (PValue param : params) {
            invoke.arg(ILExprToJava.PValueToJava(param, cm));
        }
        return invoke;
    }

}
