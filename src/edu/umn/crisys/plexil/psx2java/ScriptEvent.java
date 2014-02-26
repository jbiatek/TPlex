package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;

public interface ScriptEvent {
    
    /**
     * Create this event in Java.
     * 
     * @param cm
     */
    public JExpression toJava(JCodeModel cm);

}
