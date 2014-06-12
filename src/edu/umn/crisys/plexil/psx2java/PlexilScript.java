package edu.umn.crisys.plexil.psx2java;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.java.psx.JavaPlexilScript;

public class PlexilScript {

    
    private String scriptName;
    private List<ScriptEvent> initialEvents = new ArrayList<ScriptEvent>();
    private List<ScriptEvent> mainEvents = new ArrayList<ScriptEvent>();
    
    public PlexilScript(String name) { 
        this.scriptName = name;
    }
    
    public void addInitialEvent(ScriptEvent e) {
        initialEvents.add(e);
    }
    
    public void addMainEvent(ScriptEvent e ) {
        mainEvents.add(e);
    }
    
    public List<ScriptEvent> getInitialEvents() {
        return initialEvents;
    }
    
    public List<ScriptEvent> getMainEvents() {
        return mainEvents;
    }
    
    public void toJava(JCodeModel cm, String pkg) throws JClassAlreadyExistsException {
    	String cleanName = NameUtils.clean(scriptName);
        String fullName = pkg.equals("") ? cleanName : pkg+"."+ cleanName;
        
        JDefinedClass clazz = cm._class(fullName);
        clazz._extends(cm.ref(JavaPlexilScript.class));
        JMethod constructor = clazz.constructor(JMod.PUBLIC);
        
        // The first event is always the initial state. If we don't have one,
        // just do a Delay. 
        if (initialEvents.isEmpty()) {
        	constructor.body().invoke("addEvent").arg(DelayEvent.SINGLETON.toJava(cm));
        } else {
        	SimultaneousEvent initial = new SimultaneousEvent();
        	for (ScriptEvent e : initialEvents) {
        		initial.addEvent(e);
        	}
        	constructor.body().invoke("addEvent").arg(initial.toJava(cm));
        }
        
        for (ScriptEvent e : mainEvents) {
        	constructor.body().invoke("addEvent").arg(e.toJava(cm));
        }
        
        constructor.body().invoke("reset");
    }
    
    
}
