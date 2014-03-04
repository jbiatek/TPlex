package edu.umn.crisys.plexil.psx2java;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

public class SimultaneousEvent implements ScriptEvent {
	
	private List<ScriptEvent> events = new ArrayList<ScriptEvent>();

	public void addEvent(ScriptEvent e) {
		events.add(e);
	}
	
	public List<ScriptEvent> getEvents() {
		return events;
	}
	
	@Override
	public JExpression toJava(JCodeModel cm) {
		JInvocation constructor = JExpr.invoke("simultaneous");
		for (ScriptEvent e : events) {
			constructor.arg(e.toJava(cm));
		}
		return constructor;
	}

}
