package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;

public interface ScriptEvent {
    
    /**
     * Intended for translating to a TranslatedScript, this will add the 
     * appropriate code to create this event to the constructor block.
     * @param block The constructor block for the class extending TranslatedScript
     * @param cm
     */
    public void toJava(JBlock block, JCodeModel cm);

}
