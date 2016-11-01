package edu.umn.crisys.plexil.script.translator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.il2java.expr.ILExprToJava;
import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.script.ast.CommandAbortAck;
import edu.umn.crisys.plexil.script.ast.CommandAck;
import edu.umn.crisys.plexil.script.ast.CommandReturn;
import edu.umn.crisys.plexil.script.ast.Delay;
import edu.umn.crisys.plexil.script.ast.Event;
import edu.umn.crisys.plexil.script.ast.FunctionCall;
import edu.umn.crisys.plexil.script.ast.PlexilScript;
import edu.umn.crisys.plexil.script.ast.ScriptEventVisitor;
import edu.umn.crisys.plexil.script.ast.Simultaneous;
import edu.umn.crisys.plexil.script.ast.StateChange;
import edu.umn.crisys.plexil.script.ast.UpdateAck;
import edu.umn.crisys.util.NameUtils;

public class ScriptToJava implements ScriptEventVisitor<JCodeModel, JExpression> {
	
    public static void toJava(PlexilScript script, JCodeModel cm, String pkg) throws JClassAlreadyExistsException {
    	
    	String cleanName = NameUtils.clean(script.getScriptName());
        String fullName = pkg.equals("") ? cleanName : pkg+"."+ cleanName;
        
        JDefinedClass clazz = cm._class(fullName);
        clazz._extends(cm.ref(JavaPlexilScript.class));
        JMethod constructor = clazz.constructor(JMod.PUBLIC);
        
        // The first event is always the initial state. If we don't have one,
        // just do a Delay. 
        if (script.getInitialEvents().isEmpty()) {
        	constructor.body().invoke("addEvent").arg(Delay.SINGLETON.accept(SINGLETON, cm));
        } else {
        	Simultaneous initial = new Simultaneous();
        	for (Event e : script.getInitialEvents()) {
        		initial.addEvent(e);
        	}
        	constructor.body().invoke("addEvent").arg(initial.accept(SINGLETON, cm));
        }
        
        for (Event e : script.getMainEvents()) {
        	constructor.body().invoke("addEvent").arg(e.accept(SINGLETON, cm));
        }
        
        constructor.body().invoke("reset");
    }

	private static final ScriptToJava SINGLETON = new ScriptToJava();
	private ScriptToJava() {}

	@Override
	public JExpression visitCommandAck(CommandAck ack, JCodeModel cm) {
		return doCommandEvent(ack.getCall(), ack.getResult(), "commandAck", cm);
	}

	@Override
	public JExpression visitCommandReturn(CommandReturn ret, JCodeModel cm) {
		return doCommandEvent(ret.getCall(), ret.getValue(), "commandReturn", cm);
	}
	
	@Override
	public JExpression visitCommandAbortAck(CommandAbortAck abort, JCodeModel cm) {
		JInvocation invoke = JExpr.invoke("commandAbortAck")
				.arg(JExpr.lit(abort.getCall().getName()));
		for (PValue param : abort.getCall().getArgs()) {
			invoke.arg(ILExprToJava.PValueToJava(param, cm));
		}
		return invoke;
	}

	private JExpression doCommandEvent(FunctionCall call, PValue result, String methodToInvoke, JCodeModel cm) {
		JInvocation invoke = JExpr.invoke(methodToInvoke)
				.arg(ILExprToJava.toJava(result, cm))
				.arg(JExpr.lit(call.getName()));
		for (PValue param : call.getArgs()) {
			invoke.arg(ILExprToJava.PValueToJava(param, cm));
		}
		return invoke;

	}

	@Override
	public JExpression visitDelay(Delay d, JCodeModel cm) {
		return JExpr.invoke("delay");
	}

	@Override
	public JExpression visitSimultaneous(Simultaneous sim, JCodeModel cm) {
		JInvocation constructor = JExpr.invoke("simultaneous");
		for (Event e : sim.getEvents()) {
			constructor.arg(e.accept(this, cm));
		}
		return constructor;
	}

	@Override
	public JExpression visitStateChange(StateChange lookup, JCodeModel cm) {
        JInvocation invoke = 
        		JExpr.invoke("stateChange")
        		.arg(ILExprToJava.toJava(lookup.getValue(), cm))
        		.arg(JExpr.lit(lookup.getLookup().getName()));
        for (PValue param : lookup.getLookup().getArgs()) {
        	invoke.arg(ILExprToJava.PValueToJava(param, cm));
        }
        return invoke;
	}

	@Override
	public JExpression visitUpdateAck(UpdateAck ack, JCodeModel cm) {
        return JExpr.invoke("updateAck").arg(JExpr.lit(ack.getNodeName()));
	}

}
