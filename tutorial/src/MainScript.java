
import edu.umn.crisys.plexil.runtime.plx.*;
import edu.umn.crisys.plexil.runtime.values.*;
import edu.umn.crisys.plexil.runtime.world.*;


public class MainScript {
	
	public static void main(String[] args) {
		JavaPlan.DEBUG = true;
		
		ExternalWorld world = new TargetPanoramaTimeoutScript();
		JavaPlan plan = new TargetPanorama(world);
		// Keep doing steps until the script runs out of events.
		plan.runPlanToCompletion();
	}
}
