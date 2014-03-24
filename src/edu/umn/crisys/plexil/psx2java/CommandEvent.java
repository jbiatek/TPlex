package edu.umn.crisys.plexil.psx2java;

import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.java.values.PValue;

public class CommandEvent implements ScriptEvent {

    public static enum Action {
        RETURN("commandReturn"), 
        ACK("commandAck"), 
        ABORT("commandAbort");
        
        public String methodToCall;
        
        private Action(String method) {
            methodToCall = method;
        }
        
    }
    
    private String name;
    private Action action;
    private List<PValue> params;
    private ILExpression result;
    
    public CommandEvent(Action action, String name, List<PValue> params, ILExpression result) {
        this.name = name;
        this.action = action;
        this.params = params;
        this.result = result;
    }
    
    @Override
    public JExpression toJava(JCodeModel cm) {
        JInvocation invoke = JExpr.invoke(action.methodToCall)
            .arg(ILExprToJava.toJava(result, cm))
            .arg(JExpr.lit(name));
        for (PValue param : params) {
            invoke.arg(ILExprToJava.PValueToJava(param, cm));
        }
        return invoke;
    }

}
