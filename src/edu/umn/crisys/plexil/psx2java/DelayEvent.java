package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

public class DelayEvent implements ScriptEvent {

    @Override
    public void toJava(JBlock block, JCodeModel cm) {
        block.invoke("addDelay");
    }

}
