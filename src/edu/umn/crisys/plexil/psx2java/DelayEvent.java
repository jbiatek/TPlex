package edu.umn.crisys.plexil.psx2java;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

public class DelayEvent implements ScriptEvent {
	
	public static final DelayEvent SINGLETON = new DelayEvent();
	
	private DelayEvent() {}

    @Override
    public JExpression toJava(JCodeModel cm) {
        return JExpr.invoke("delay");
    }

}
