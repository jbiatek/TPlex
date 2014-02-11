package edu.umn.crisys.plexil.psx2java;

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.java.values.PValue;
import edu.umn.crisys.plexil.translator.il.ILExprToJava;

public class CommandEvent implements ScriptEvent {

    public static enum Action {
        RETURN("addCommandReturn"), 
        ACK("addCommandAck"), 
        ABORT("addCommandAbort");
        
        public String methodToCall;
        
        private Action(String method) {
            methodToCall = method;
        }
        
    }
    
    private String name;
    private Action action;
    private List<PValue> params;
    private PValue result;
    
    public CommandEvent(Action action, String name, List<PValue> params, PValue result) {
        this.name = name;
        this.action = action;
        this.params = params;
        this.result = result;
    }
    
    @Override
    public void toJava(JBlock block, JCodeModel cm) {
        JInvocation invoke = block.invoke(action.methodToCall)
            .arg(ILExprToJava.PValueToJava(result, cm))
            .arg(JExpr.lit(name));
        for (PValue param : params) {
            invoke.arg(ILExprToJava.PValueToJava(param, cm));
        }
        
    }

}
