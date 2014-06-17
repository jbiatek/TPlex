package edu.umn.crisys.plexil.script.ast;

import java.util.ArrayList;
import java.util.List;

public class PlexilScript {

    
    private String scriptName;
    private List<Event> initialEvents = new ArrayList<Event>();
    private List<Event> mainEvents = new ArrayList<Event>();
    
    public PlexilScript(String name) { 
        this.scriptName = name;
    }
    
    public void addInitialEvent(Event e) {
        initialEvents.add(e);
    }
    
    public void addMainEvent(Event e ) {
        mainEvents.add(e);
    }
    
    public List<Event> getInitialEvents() {
        return initialEvents;
    }
    
    public List<Event> getMainEvents() {
        return mainEvents;
    }
    
    public String getScriptName() {
    	return scriptName;
    }
}
