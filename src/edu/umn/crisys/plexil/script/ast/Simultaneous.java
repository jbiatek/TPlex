package edu.umn.crisys.plexil.script.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Simultaneous implements Event {
	private List<Event> events;
	
	public Simultaneous(Event...events) {
		this(new ArrayList<Event>(Arrays.asList(events)));
	}

	public Simultaneous(List<Event> events) {
		this.events = events;
	}

	public void addEvent(Event e) {
		events.add(e);
	}
	
	public List<Event> getEvents() {
		return events;
	}
	
	/**
	 * Reduce this event down into the simplest thing that is still equivalent.
	 * For example, if this is an empty Simultaneous event, a Delay will be
	 * returned. If it only contains one item, that item will be returned. 
	 * @return
	 */
	public Event getCleanestEvent() {
		if (events.isEmpty()) {
			return Delay.SINGLETON;
		}
		if (events.size() == 1) {
			return events.get(0);
		} 
		return this;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Simultaneous) {
			Simultaneous o = (Simultaneous) other;
			
			Set<Event> mySet = new HashSet<Event>(events);
			Set<Event> theirSet = new HashSet<Event>(o.events);
			
			return mySet.equals(theirSet);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashSet<Event>(events).hashCode();
	}

	@Override
	public <P, R> R accept(ScriptEventVisitor<P, R> v, P param) {
		return v.visitSimultaneous(this, param);
	}
	
}