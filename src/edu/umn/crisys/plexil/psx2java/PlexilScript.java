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
        
        for (ScriptEvent e : initialEvents) {
        	constructor.body().invoke("addEvent").arg(e.toJava(cm));
        }
        // Those events happen immediately, so just do them in the constructor
        constructor.body().invoke("performAllEventsInQueue");
        
        for (ScriptEvent e : mainEvents) {
        	constructor.body().invoke("addEvent").arg(e.toJava(cm));
        }
        
    }
    
    
}
