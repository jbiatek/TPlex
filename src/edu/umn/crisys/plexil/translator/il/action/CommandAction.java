package edu.umn.crisys.plexil.translator.il.action;

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;

import edu.umn.crisys.plexil.ast.core.expr.ILExpression;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.translator.il.ILExprToJava;
import edu.umn.crisys.plexil.translator.il.vars.CommandHandleReference;

public class CommandAction implements PlexilAction {

    private CommandHandleReference handle;
    private ILExpression name;
    private List<ILExpression> args;
    
    public CommandAction(CommandHandleReference handle, ILExpression name, 
            List<ILExpression> args) {
        this.handle = handle;
        this.name = name;
        PlexilType.STRING.typeCheck(name.getType());
        this.args = args;
    }
    
    public List<ILExpression> getArgs() {
        return args;
    }
    
    @Override
    public String toString() {
        String ret = "Issue command: ";
        ret += name + "(";
        for (ILExpression arg : args) {
            ret += arg + ", ";
        }
        return ret.substring(0, ret.length() - 2) +")";
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        //ex.command(caller, name, args);
        JInvocation cmdCall = 
        block.invoke(JExpr.invoke("getWorld"), "command")
            .arg(handle.directReference(cm))
            .arg(ILExprToJava.toJava(name, cm));
        block.invoke("endMacroStep");
        for (ILExpression arg : args) {
            cmdCall.arg(ILExprToJava.toJava(arg, cm));
        }
    }
    
}
