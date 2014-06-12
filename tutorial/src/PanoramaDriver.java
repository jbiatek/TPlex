
import edu.umn.crisys.plexil.java.PlexilDriver;
import edu.umn.crisys.plexil.java.TestGenerationInfo;
import edu.umn.crisys.plexil.java.plx.*;
import edu.umn.crisys.plexil.java.psx.symbolic.*;
import edu.umn.crisys.plexil.java.world.*;
import edu.umn.crisys.plexil.java.values.*;


public class PanoramaDriver implements TestGenerationInfo {
	
	
	public static void main(String[] args) throws Exception {
		PlexilDriver.mainMethod(new PanoramaDriver(), args);
	}
	
	@Override
	public JavaPlan createPlanUnderTest(ExternalWorld world) {
		return new TargetPanorama(world);
	}
	@Override
	public SymbolicDecisionMaker createDecisionMaker(ValueSource values) {
		SymbolicDecisionMaker search = new CompleteSearchStrategy(values);
		// Respond to all the commands with either success or failure
		search.addCommand("rover_drive", CommandHandleState.COMMAND_SUCCESS,
				CommandHandleState.COMMAND_FAILED);
		search.addCommand("rover_stop", CommandHandleState.COMMAND_SUCCESS,
				CommandHandleState.COMMAND_FAILED);
		search.addCommand("take_navcam", CommandHandleState.COMMAND_SUCCESS,
				CommandHandleState.COMMAND_FAILED);
		search.addCommand("take_pancam", CommandHandleState.COMMAND_SUCCESS,
				CommandHandleState.COMMAND_FAILED);
		// The Lookup(target_in_view) should return a boolean.
		search.addLookup("target_in_view", PlexilType.BOOLEAN);
		// Time should be an integer.
		search.addIncreasingLookup("time", 0);
		return search;
	}
	@Override
	public ValueSource createSymbolicValueSource() {
		return new SPFValues();
	}
}
