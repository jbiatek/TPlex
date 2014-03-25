package edu.umn.crisys.plexil.il.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeAction implements PlexilAction {
	
	private List<PlexilAction> actions;
	
	public CompositeAction(PlexilAction... actions) {
		this.actions = Arrays.asList(actions);
	}
	
	public CompositeAction(List<PlexilAction> actions) {
		this.actions = new ArrayList<PlexilAction>(actions);
	}
	
	public List<PlexilAction> getActions() {
		return actions;
	}

	@Override
	public <P, R> R accept(ILActionVisitor<P, R> visitor, P param) {
		return visitor.visitComposite(this, param);
	}

}
