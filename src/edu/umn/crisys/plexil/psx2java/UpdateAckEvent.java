package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

public class UpdateAckEvent implements ScriptEvent {

    private String updateName;
    
    public UpdateAckEvent(String name) {
        this.updateName = name;
    }
    
    @Override
    public JExpression toJava(JCodeModel cm) {
        return JExpr.invoke("updateAck").arg(JExpr.lit(updateName));
    }

}
