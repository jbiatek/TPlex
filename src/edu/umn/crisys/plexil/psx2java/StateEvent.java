package edu.umn.crisys.plexil.psx2java;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.translator.il.ILExprToJava;

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
    public void toJava(JBlock block, JCodeModel cm) {
        JInvocation invoke = 
            block.invoke("addStateChange")
                .arg(ILExprToJava.toJava(returnValue, cm))
                .arg(JExpr.lit(name));
        for (PValue param : params) {
            invoke.arg(ILExprToJava.PValueToJava(param, cm));
        }

    }

}
