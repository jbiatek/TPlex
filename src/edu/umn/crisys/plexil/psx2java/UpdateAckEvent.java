package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;

public class UpdateAckEvent implements ScriptEvent {

    private String updateName;
    
    public UpdateAckEvent(String name) {
        this.updateName = name;
    }
    
    @Override
    public void toJava(JBlock block, JCodeModel cm) {
        block.invoke("addUpdateAck").arg(JExpr.lit(updateName));
    }

}
