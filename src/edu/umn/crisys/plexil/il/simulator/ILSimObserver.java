package edu.umn.crisys.plexil.il.simulator;

import edu.umn.crisys.plexil.il.expr.vars.SimpleVar;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.world.CommandHandler;

public interface ILSimObserver extends JavaPlanObserver {

	
	public default void transitionTaken(Transition t) {
		
	}
	
	public default void specialCommand(SimpleVar simpleVar, 
			CommandHandler handle, PString commandName, PValue... args) {
		
	}

}
