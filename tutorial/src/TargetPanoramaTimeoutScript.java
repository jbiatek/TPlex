import edu.umn.crisys.plexil.runtime.psx.JavaPlexilScript;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.RealValue;

public class TargetPanoramaTimeoutScript
    extends JavaPlexilScript
{


    public TargetPanoramaTimeoutScript() {
        addEvent(simultaneous(stateChange(RealValue.get((0.0)), "time"), stateChange(BooleanValue.get((false)), "target_in_view")));
        addEvent(stateChange(RealValue.get((1.0)), "time"));
        addEvent(stateChange(RealValue.get((2.0)), "time"));
        addEvent(commandAck(CommandHandleState.COMMAND_SUCCESS, "rover_drive", RealValue.get((10.0))));
        addEvent(stateChange(RealValue.get((3.0)), "time"));
        addEvent(stateChange(RealValue.get((4.0)), "time"));
        addEvent(stateChange(RealValue.get((5.0)), "time"));
        addEvent(stateChange(RealValue.get((6.0)), "time"));
        addEvent(stateChange(RealValue.get((7.0)), "time"));
        addEvent(stateChange(RealValue.get((8.0)), "time"));
        addEvent(stateChange(RealValue.get((9.0)), "time"));
        addEvent(stateChange(RealValue.get((10.0)), "time"));
        addEvent(commandAck(CommandHandleState.COMMAND_SUCCESS, "rover_stop"));
        addEvent(commandAck(CommandHandleState.COMMAND_SUCCESS, "take_navcam"));
        addEvent(stateChange(RealValue.get((11.0)), "time"));
        reset();
    }

}
