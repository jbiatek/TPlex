
import edu.umn.crisys.plexil.java.plx.*;
import edu.umn.crisys.plexil.java.world.*;
import edu.umn.crisys.plexil.java.values.*;


public class MainScript {
	
	public static void main(String[] args) {
		JavaPlan.DEBUG = true;
		
		ExternalWorld world = new TargetPanoramaTimeoutScript();
		JavaPlan plan = new TargetPanorama(world);
		// Keep doing steps until the script runs out of events.
		while ( ! world.stop() ) {
			plan.doMacroStep();
			world.waitForNextEvent();
		}
	}
}
