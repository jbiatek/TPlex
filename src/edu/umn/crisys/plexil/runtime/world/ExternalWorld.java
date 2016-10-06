package edu.umn.crisys.plexil.runtime.world;

import edu.umn.crisys.plexil.runtime.plx.JavaPlanObserver;
import edu.umn.crisys.plexil.runtime.values.PReal;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;

public interface ExternalWorld extends JavaPlanObserver {

	/**
	 * Notify the executive that no more changes are forthcoming. For example,
	 * a script has run out of things to do. 
	 * @return true to indicate that the script should probably be stopped soon.
	 */
	public boolean done();
	
	/**
	 * Receive an update from the Plexil program.
	 * @param node The node that issued the update
	 * @param key
	 * @param value
	 */
	public void update(UpdateHandler node, String key, PValue value);
	
	/**
	 * Perform a lookup which returns immediately.
	 * @param stateName
	 * @param args
	 */
	public PValue lookupNow(PString stateName, PValue... args );
	
	
	/**
	 * Perform an asynchronous lookup. 
	 * @param stateName
	 * @param tolerance
	 * @param args
	 */
	public PValue lookupOnChange(PString stateName, PReal tolerance, 
			PValue... args);
	
	/**
	 * Send a command out to the external system.
	 * 
	 * @param caller The node which sent the command.
	 * @param name The command name.
	 * @param args The arguments.
	 */
	public void command(CommandHandler caller, PString name, PValue... args);
}
