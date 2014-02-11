package edu.umn.crisys.plexil.translator.il.action;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

import edu.umn.crisys.plexil.translator.il.vars.CommandHandleReference;

public class AbortCommandAction implements PlexilAction {

    private CommandHandleReference handle;
    
    public AbortCommandAction(CommandHandleReference handle) {
        this.handle = handle;
    }

    @Override
    public void addActionToBlock(JBlock block, JCodeModel cm) {
        block._throw(JExpr._new(cm.ref(RuntimeException.class)).arg("Aborting commands isn't supported yet"));
    }
    
    @Override
    public String toString() {
        return "Action: Abort command for handle "+handle;
    }
    
}
