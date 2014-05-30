import edu.umn.crisys.plexil.java.plx.CommandHandle;
import edu.umn.crisys.plexil.java.plx.JavaPlan;
import edu.umn.crisys.plexil.java.plx.LibraryInterface;
import edu.umn.crisys.plexil.java.plx.SimpleCurrentNext;
import edu.umn.crisys.plexil.java.plx.SimplePValue;
import edu.umn.crisys.plexil.java.values.BooleanValue;
import edu.umn.crisys.plexil.java.values.CommandHandleState;
import edu.umn.crisys.plexil.java.values.IntegerValue;
import edu.umn.crisys.plexil.java.values.NodeFailureType;
import edu.umn.crisys.plexil.java.values.NodeOutcome;
import edu.umn.crisys.plexil.java.values.NodeState;
import edu.umn.crisys.plexil.java.values.PBoolean;
import edu.umn.crisys.plexil.java.values.PNumeric;
import edu.umn.crisys.plexil.java.values.PlexilType;
import edu.umn.crisys.plexil.java.values.RealValue;
import edu.umn.crisys.plexil.java.values.StringValue;
import edu.umn.crisys.plexil.java.values.UnknownValue;
import edu.umn.crisys.plexil.java.world.ExternalWorld;
import edu.umn.crisys.plexil.test.java.PlanState;
import edu.umn.crisys.plexil.test.java.PlexilTestable;

public class TargetPanorama
    extends JavaPlan
    implements PlexilTestable
{

    private SimplePValue<PBoolean> DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value = new SimplePValue<PBoolean>(UnknownValue.get(), PlexilType.BOOLEAN);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTimeout____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeOutcome> DriveToTarget____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<PBoolean> DriveToTarget__drive_done = new SimplePValue<PBoolean>(BooleanValue.get((false)), PlexilType.BOOLEAN);
    private SimplePValue<NodeFailureType> DriveToTarget__TakeNavcam____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<CommandHandleState> DriveToTarget__StopForTarget__Stop____command_handle = new SimplePValue<CommandHandleState>(CommandHandleState.UNKNOWN, PlexilType.COMMAND_HANDLE);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTarget__SetDriveFlag____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeFailureType> DriveToTarget____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<CommandHandleState> DriveToTarget__TakePancam____command_handle = new SimplePValue<CommandHandleState>(CommandHandleState.UNKNOWN, PlexilType.COMMAND_HANDLE);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTarget__Stop____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeOutcome> DriveToTarget__Drive____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTimeout__SetTimeoutFlag____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTarget__SetDriveFlag____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<CommandHandleState> DriveToTarget__StopForTimeout__Stop____command_handle = new SimplePValue<CommandHandleState>(CommandHandleState.UNKNOWN, PlexilType.COMMAND_HANDLE);
    private SimplePValue<NodeOutcome> DriveToTarget__TakeNavcam____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTarget____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTimeout__Stop____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeFailureType> DriveToTarget__StopForTimeout____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<CommandHandleState> DriveToTarget__TakeNavcam____command_handle = new SimplePValue<CommandHandleState>(CommandHandleState.UNKNOWN, PlexilType.COMMAND_HANDLE);
    private SimplePValue<PBoolean> DriveToTarget__timeout = new SimplePValue<PBoolean>(BooleanValue.get((false)), PlexilType.BOOLEAN);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTimeout__Stop____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTarget____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeFailureType> DriveToTarget__Drive____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeFailureType> DriveToTarget__TakePancam____failure = new SimplePValue<NodeFailureType>(NodeFailureType.UNKNOWN, PlexilType.FAILURE);
    private SimplePValue<NodeOutcome> DriveToTarget__TakePancam____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<NodeOutcome> DriveToTarget__StopForTarget__Stop____outcome = new SimplePValue<NodeOutcome>(NodeOutcome.UNKNOWN, PlexilType.OUTCOME);
    private SimplePValue<PBoolean> DriveToTarget__StopForTarget__SetDriveFlag____previous_value = new SimplePValue<PBoolean>(UnknownValue.get(), PlexilType.BOOLEAN);
    private SimplePValue<CommandHandleState> DriveToTarget__Drive____command_handle = new SimplePValue<CommandHandleState>(CommandHandleState.UNKNOWN, PlexilType.COMMAND_HANDLE);
    private SimpleCurrentNext<Integer> DriveToTarget__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__Drive__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTimeout__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTimeout__Stop__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTimeout__SetTimeoutFlag__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTarget__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTarget__Stop__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__StopForTarget__SetDriveFlag__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__TakeNavcam__state = new SimpleCurrentNext<Integer>(0);
    private SimpleCurrentNext<Integer> DriveToTarget__TakePancam__state = new SimpleCurrentNext<Integer>(0);

    public TargetPanorama(ExternalWorld world) {
        super(world);
    }

    public TargetPanorama(LibraryInterface inParent) {
        super(inParent);
    }

    void MicroStep___DriveToTarget() {
        switch (DriveToTarget__state.getCurrent()) {
            case  0 :
                if (getInterface().evalParentState().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (<root node's parent state> == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1fa4f2fd ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalParentState().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (<root node's parent state> == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@704aa085 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3370b55b ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget____outcome);
                        DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget____failure);
                        DriveToTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    } else {
                        if (getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (<root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2c9283bf ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget____outcome);
                            DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget____failure);
                            DriveToTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : WAITING (6) -> EXECUTING");
                            }
                            DriveToTarget__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@47078775 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3f4e36c1 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget____outcome);
                        DriveToTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget____failure);
                        DriveToTarget__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    } else {
                        if ((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue()) {
                            /*
(State #2) priority 5 ----> 
DriveToTarget : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state)
 ----> (State #3)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : EXECUTING (5) -> FINISHING");
                            }
                            DriveToTarget__state.setNext(3);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@307f7fe ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2a4640 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget____outcome);
                        DriveToTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget____failure);
                        DriveToTarget__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    } else {
                        if (((((STATE___DriveToTarget__Drive().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__Drive().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakePancam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakePancam().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget : FINISHING (5) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__Drive.state == WAITING || DriveToTarget__Drive.state == FINISHED && DriveToTarget__StopForTimeout.state == WAITING || DriveToTarget__StopForTimeout.state == FINISHED && DriveToTarget__StopForTarget.state == WAITING || DriveToTarget__StopForTarget.state == FINISHED && DriveToTarget__TakeNavcam.state == WAITING || DriveToTarget__TakeNavcam.state == FINISHED && DriveToTarget__TakePancam.state == WAITING || DriveToTarget__TakePancam.state == FINISHED)
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@576a13d5 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget____outcome);
                            DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget____failure);
                            DriveToTarget__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@753c31c8 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@42e87d99 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget____outcome);
                        DriveToTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget____failure);
                        DriveToTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    } else {
                        if (getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (<root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&(((((STATE___DriveToTarget__Drive().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__Drive().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakePancam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakePancam().equalTo(NodeState.FINISHED).isTrue()))) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__Drive.state == WAITING || DriveToTarget__Drive.state == FINISHED && DriveToTarget__StopForTimeout.state == WAITING || DriveToTarget__StopForTimeout.state == FINISHED && DriveToTarget__StopForTarget.state == WAITING || DriveToTarget__StopForTarget.state == FINISHED && DriveToTarget__TakeNavcam.state == WAITING || DriveToTarget__TakeNavcam.state == FINISHED && DriveToTarget__TakePancam.state == WAITING || DriveToTarget__TakePancam.state == FINISHED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&(((((STATE___DriveToTarget__Drive().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__Drive().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakePancam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakePancam().equalTo(NodeState.FINISHED).isTrue()))) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__Drive.state == WAITING || DriveToTarget__Drive.state == FINISHED && DriveToTarget__StopForTimeout.state == WAITING || DriveToTarget__StopForTimeout.state == FINISHED && DriveToTarget__StopForTarget.state == WAITING || DriveToTarget__StopForTarget.state == FINISHED && DriveToTarget__TakeNavcam.state == WAITING || DriveToTarget__TakeNavcam.state == FINISHED && DriveToTarget__TakePancam.state == WAITING || DriveToTarget__TakePancam.state == FINISHED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__state);
                        changeOccurred();
                    } else {
                        if (((((STATE___DriveToTarget__Drive().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__Drive().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakeNavcam().equalTo(NodeState.FINISHED).isTrue()))&&(STATE___DriveToTarget__TakePancam().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__TakePancam().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget : FAILING (3) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__Drive.state == WAITING || DriveToTarget__Drive.state == FINISHED && DriveToTarget__StopForTimeout.state == WAITING || DriveToTarget__StopForTimeout.state == FINISHED && DriveToTarget__StopForTarget.state == WAITING || DriveToTarget__StopForTarget.state == FINISHED && DriveToTarget__TakeNavcam.state == WAITING || DriveToTarget__TakeNavcam.state == FINISHED && DriveToTarget__TakePancam.state == WAITING || DriveToTarget__TakePancam.state == FINISHED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (getInterface().evalParentState().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (<root node's parent state> == WAITING)
[ Reset variables: .outcome, drive_done, .failure, timeout, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget____outcome);
                    DriveToTarget__drive_done.setNext(BooleanValue.get((false)));
                    commitAfterMicroStep(DriveToTarget__drive_done);
                    DriveToTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget____failure);
                    DriveToTarget__timeout.setNext(BooleanValue.get((false)));
                    commitAfterMicroStep(DriveToTarget__timeout);
                    DriveToTarget__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__state.getNext()) {
            case  1 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
            case  2 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
            case  3 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
            case  4 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
            case  5 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
            case  6 :
                MicroStep___DriveToTarget__TakePancam();
                MicroStep___DriveToTarget__TakeNavcam();
                MicroStep___DriveToTarget__Drive();
                MicroStep___DriveToTarget__StopForTarget();
                MicroStep___DriveToTarget__StopForTimeout();
                break;
        }
    }

    public NodeState STATE___DriveToTarget() {
        switch (DriveToTarget__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget");
    }

    void MicroStep___DriveToTarget__Drive() {
        switch (DriveToTarget__Drive__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__Drive : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1ce34b50 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__Drive : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__Drive__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__Drive : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@e658001 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__Drive : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@29b6c909 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__Drive____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__Drive____outcome);
                        DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__Drive____failure);
                        DriveToTarget__Drive__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__Drive : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1f488344 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__Drive____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__Drive____outcome);
                            DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__Drive____failure);
                            DriveToTarget__Drive__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget__Drive : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : WAITING (6) -> EXECUTING");
                            }
                            getWorld().command(new CommandHandle(DriveToTarget__Drive____command_handle), StringValue.get(("rover_drive")), IntegerValue.get((10)));
                            endMacroStep();
                            DriveToTarget__Drive__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__Drive : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3452a22d ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__Drive : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1d155331 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__Drive____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__Drive____outcome);
                        DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__Drive____failure);
                        DriveToTarget__Drive__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__Drive : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (true || .command_handle == COMMAND_DENIED || .command_handle == COMMAND_FAILED)
 ----> (State #3)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : EXECUTING (5) -> FINISHING");
                        }
                        DriveToTarget__Drive__state.setNext(3);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__Drive : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6308c4e1 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__Drive : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@54b5c292 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__Drive____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__Drive____outcome);
                        DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__Drive____failure);
                        DriveToTarget__Drive__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    } else {
                        if (BooleanValue.get(DriveToTarget__Drive____command_handle.getCurrent().isKnown()).isTrue()) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__Drive : FINISHING (5) -> ITERATION_ENDED
<COMMAND_ACCEPTED T?> (isKnown(.command_handle))
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@68f92ab0 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__Drive____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__Drive____outcome);
                            DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__Drive____failure);
                            DriveToTarget__Drive__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__Drive : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3e61bea7 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__Drive : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@32be5f0 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__Drive____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__Drive____outcome);
                        DriveToTarget__Drive____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__Drive____failure);
                        DriveToTarget__Drive__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__Drive : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__Drive__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__Drive : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__Drive__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__Drive____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&DriveToTarget__Drive____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__Drive : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__Drive__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__Drive____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&DriveToTarget__Drive____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__Drive : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__Drive : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__Drive__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__Drive__state);
                        changeOccurred();
                    } else {
                        if (DriveToTarget__Drive____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__Drive : FAILING (3) -> ITERATION_ENDED
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__Drive : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__Drive__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__Drive__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__Drive : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget.state == WAITING)
[ Reset variables: .outcome, .command_handle, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__Drive : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__Drive____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__Drive____outcome);
                    DriveToTarget__Drive____command_handle.setNext(CommandHandleState.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__Drive____command_handle);
                    DriveToTarget__Drive____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__Drive____failure);
                    DriveToTarget__Drive__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__Drive__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__Drive__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__Drive() {
        switch (DriveToTarget__Drive__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__Drive");
    }

    void MicroStep___DriveToTarget__StopForTimeout() {
        switch (DriveToTarget__StopForTimeout__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3704f130 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTimeout__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTimeout : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2576a288 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTimeout : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@fa8a1ef ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                        DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                        DriveToTarget__StopForTimeout__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTimeout : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@81f59f1 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                            DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                            DriveToTarget__StopForTimeout__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        } else {
                            if (((PNumeric) getWorld().lookupOnChange(StringValue.get(("time")), RealValue.get((0.1)))).ge(IntegerValue.get((10))).isTrue()) {
                                /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTimeout : WAITING (6) -> EXECUTING
<START_CONDITION T?> ((PNumeric) (LookupOnChange(time, 0.1)) >= 10)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                                if (JavaPlan.DEBUG) {
                                    System.out.println("DriveToTarget__StopForTimeout : WAITING (6) -> EXECUTING");
                                }
                                DriveToTarget__StopForTimeout__state.setNext(2);
                                commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                                changeOccurred();
                            }
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTimeout : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@195d4f45 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTimeout : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3cff4805 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                        DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                        DriveToTarget__StopForTimeout__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    } else {
                        if (NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag()).isTrue()) {
                            /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTimeout : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (FINISHED == DriveToTarget__StopForTimeout__Stop.state && FINISHED == DriveToTarget__StopForTimeout__SetTimeoutFlag.state)
 ----> (State #3)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : EXECUTING (5) -> FINISHING");
                            }
                            DriveToTarget__StopForTimeout__state.setNext(3);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__StopForTimeout : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1bd15f8c ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__StopForTimeout : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4ed7ee98 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                        DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                        DriveToTarget__StopForTimeout__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    } else {
                        if ((STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__StopForTimeout : FINISHING (5) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTimeout__Stop.state == WAITING || DriveToTarget__StopForTimeout__Stop.state == FINISHED && DriveToTarget__StopForTimeout__SetTimeoutFlag.state == WAITING || DriveToTarget__StopForTimeout__SetTimeoutFlag.state == FINISHED)
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@42716598 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                            DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                            DriveToTarget__StopForTimeout__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTimeout : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@339274f9 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTimeout : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@543da1e2 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                        DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                        DriveToTarget__StopForTimeout__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTimeout : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTimeout : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTimeout____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&((STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.FINISHED).isTrue()))) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTimeout : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTimeout__Stop.state == WAITING || DriveToTarget__StopForTimeout__Stop.state == FINISHED && DriveToTarget__StopForTimeout__SetTimeoutFlag.state == WAITING || DriveToTarget__StopForTimeout__SetTimeoutFlag.state == FINISHED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTimeout____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&((STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.FINISHED).isTrue()))) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTimeout : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTimeout__Stop.state == WAITING || DriveToTarget__StopForTimeout__Stop.state == FINISHED && DriveToTarget__StopForTimeout__SetTimeoutFlag.state == WAITING || DriveToTarget__StopForTimeout__SetTimeoutFlag.state == FINISHED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                        changeOccurred();
                    } else {
                        if ((STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTimeout : FAILING (3) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTimeout__Stop.state == WAITING || DriveToTarget__StopForTimeout__Stop.state == FINISHED && DriveToTarget__StopForTimeout__SetTimeoutFlag.state == WAITING || DriveToTarget__StopForTimeout__SetTimeoutFlag.state == FINISHED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTimeout__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTimeout : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget.state == WAITING)
[ Reset variables: .outcome, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTimeout____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____outcome);
                    DriveToTarget__StopForTimeout____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout____failure);
                    DriveToTarget__StopForTimeout__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTimeout__state.getNext()) {
            case  1 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
            case  2 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
            case  3 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
            case  4 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
            case  5 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
            case  6 :
                MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag();
                MicroStep___DriveToTarget__StopForTimeout__Stop();
                break;
        }
    }

    public NodeState STATE___DriveToTarget__StopForTimeout() {
        switch (DriveToTarget__StopForTimeout__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTimeout");
    }

    void MicroStep___DriveToTarget__StopForTimeout__Stop() {
        switch (DriveToTarget__StopForTimeout__Stop__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget__StopForTimeout.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@9372bdb ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget__StopForTimeout.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTimeout__Stop__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5203b97 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTimeout__Stop : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@7283922 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                        DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                        DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTimeout__Stop : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTimeout__Stop.state && FINISHED == DriveToTarget__StopForTimeout__SetTimeoutFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@61beead9 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                            DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                            DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTimeout__Stop : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : WAITING (6) -> EXECUTING");
                            }
                            getWorld().command(new CommandHandle(DriveToTarget__StopForTimeout__Stop____command_handle), StringValue.get(("rover_stop")));
                            endMacroStep();
                            DriveToTarget__StopForTimeout__Stop__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3d2c7eb1 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTimeout__Stop : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3431b5d7 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                        DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                        DriveToTarget__StopForTimeout__Stop__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTimeout__Stop : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (true || .command_handle == COMMAND_DENIED || .command_handle == COMMAND_FAILED)
 ----> (State #3)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : EXECUTING (5) -> FINISHING");
                        }
                        DriveToTarget__StopForTimeout__Stop__state.setNext(3);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2b995106 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__StopForTimeout__Stop : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5e967746 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                        DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                        DriveToTarget__StopForTimeout__Stop__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    } else {
                        if (BooleanValue.get(DriveToTarget__StopForTimeout__Stop____command_handle.getCurrent().isKnown()).isTrue()) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__StopForTimeout__Stop : FINISHING (5) -> ITERATION_ENDED
<COMMAND_ACCEPTED T?> (isKnown(.command_handle))
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6870c52d ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                            DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                            DriveToTarget__StopForTimeout__Stop__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@43a142ec ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1030b6a4 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                        DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                        DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTimeout__Stop.state && FINISHED == DriveToTarget__StopForTimeout__SetTimeoutFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTimeout__Stop____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&DriveToTarget__StopForTimeout__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTimeout__Stop____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&DriveToTarget__StopForTimeout__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTimeout__Stop : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__Stop : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                        changeOccurred();
                    } else {
                        if (DriveToTarget__StopForTimeout__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTimeout__Stop : FAILING (3) -> ITERATION_ENDED
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__Stop : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTimeout__Stop__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTimeout__Stop : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget__StopForTimeout.state == WAITING)
[ Reset variables: .outcome, .command_handle, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__Stop : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTimeout__Stop____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____outcome);
                    DriveToTarget__StopForTimeout__Stop____command_handle.setNext(CommandHandleState.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____command_handle);
                    DriveToTarget__StopForTimeout__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop____failure);
                    DriveToTarget__StopForTimeout__Stop__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__Stop__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTimeout__Stop__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__StopForTimeout__Stop() {
        switch (DriveToTarget__StopForTimeout__Stop__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTimeout__Stop");
    }

    void MicroStep___DriveToTarget__StopForTimeout__SetTimeoutFlag() {
        switch (DriveToTarget__StopForTimeout__SetTimeoutFlag__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget__StopForTimeout.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6e26d149 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget__StopForTimeout.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3676481e ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@f78c581 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTimeout__Stop.state && FINISHED == DriveToTarget__StopForTimeout__SetTimeoutFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@41eea016 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                            DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                            DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : WAITING (6) -> EXECUTING");
                            }
                            if (JavaPlan.DEBUG) {
                                System.out.println(("timeout: New value set to "+ BooleanValue.get((true))));
                            }
                            DriveToTarget__timeout.setNext(BooleanValue.get((true)));
                            commitAfterMicroStep(DriveToTarget__timeout);
                            DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.setNext(DriveToTarget__timeout.getCurrent());
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value);
                            endMacroStep();
                            DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1950262b ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                    if (JavaPlan.DEBUG) {
                        System.out.println(("timeout: New value set to "+ DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.getCurrent()));
                    }
                    DriveToTarget__timeout.setNext(DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.getCurrent());
                    commitAfterMicroStep(DriveToTarget__timeout);
                    endMacroStep();
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5cf9de08 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                        if (JavaPlan.DEBUG) {
                            System.out.println(("timeout: New value set to "+ DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.getCurrent()));
                        }
                        DriveToTarget__timeout.setNext(DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.getCurrent());
                        commitAfterMicroStep(DriveToTarget__timeout);
                        endMacroStep();
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (5) -> ITERATION_ENDED
<END_CONDITION T?> (true)
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@18600ee8 ]
 ----> (State #4)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : EXECUTING (5) -> ITERATION_ENDED");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.SUCCESS);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(4);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4a72add8 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2ef36617 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTimeout__Stop.state && FINISHED == DriveToTarget__StopForTimeout__SetTimeoutFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    } else {
                        /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (3) -> ITERATION_ENDED
 ----> (State #4)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : FAILING (3) -> ITERATION_ENDED");
                        }
                        DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(4);
                        commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget__StopForTimeout().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTimeout__SetTimeoutFlag : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget__StopForTimeout.state == WAITING)
[ Reset variables: .outcome, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTimeout__SetTimeoutFlag : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag____failure);
                    DriveToTarget__StopForTimeout__SetTimeoutFlag__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTimeout__SetTimeoutFlag__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTimeout__SetTimeoutFlag__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag() {
        switch (DriveToTarget__StopForTimeout__SetTimeoutFlag__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTimeout__SetTimeoutFlag");
    }

    void MicroStep___DriveToTarget__StopForTarget() {
        switch (DriveToTarget__StopForTarget__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6db38815 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTarget__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTarget : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@25ab52f7 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTarget : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@12d3e973 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                        DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                        DriveToTarget__StopForTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTarget : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@168386ad ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                            DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                            DriveToTarget__StopForTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        } else {
                            if (DriveToTarget__timeout.getCurrent().isTrue()) {
                                /*
(State #1) priority 5 ----> 
DriveToTarget__StopForTarget : WAITING (5) -> FINISHED
<SKIP_CONDITION T?> (timeout)
[ edu.umn.crisys.plexil.il.action.CompositeAction@14fed367 ]
 ----> (State #6)
*/
                                if (JavaPlan.DEBUG) {
                                    System.out.println("DriveToTarget__StopForTarget : WAITING (5) -> FINISHED");
                                }
                                DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SKIPPED);
                                commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                                DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                                commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                                DriveToTarget__StopForTarget__state.setNext(6);
                                commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                                changeOccurred();
                            } else {
                                if (((PBoolean) getWorld().lookupOnChange(StringValue.get(("target_in_view")), RealValue.get((0.0)))).isTrue()) {
                                    /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTarget : WAITING (6) -> EXECUTING
<START_CONDITION T?> ((PBoolean) (LookupOnChange(target_in_view, 0.0)))
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                                    if (JavaPlan.DEBUG) {
                                        System.out.println("DriveToTarget__StopForTarget : WAITING (6) -> EXECUTING");
                                    }
                                    DriveToTarget__StopForTarget__state.setNext(2);
                                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                                    changeOccurred();
                                }
                            }
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTarget : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@65e919f5 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTarget : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2aa75818 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                        DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                        DriveToTarget__StopForTarget__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    } else {
                        if (NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__SetDriveFlag()).isTrue()) {
                            /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTarget : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (FINISHED == DriveToTarget__StopForTarget__Stop.state && FINISHED == DriveToTarget__StopForTarget__SetDriveFlag.state)
 ----> (State #3)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : EXECUTING (5) -> FINISHING");
                            }
                            DriveToTarget__StopForTarget__state.setNext(3);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__StopForTarget : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5088a588 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__StopForTarget : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3b2bad06 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                        DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                        DriveToTarget__StopForTarget__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    } else {
                        if ((STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__StopForTarget : FINISHING (5) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTarget__Stop.state == WAITING || DriveToTarget__StopForTarget__Stop.state == FINISHED && DriveToTarget__StopForTarget__SetDriveFlag.state == WAITING || DriveToTarget__StopForTarget__SetDriveFlag.state == FINISHED)
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@306c8343 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                            DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                            DriveToTarget__StopForTarget__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTarget : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@241dbc8b ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTarget : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1c455c33 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                        DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                        DriveToTarget__StopForTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTarget : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTarget : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTarget____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&((STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.FINISHED).isTrue()))) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTarget : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTarget__Stop.state == WAITING || DriveToTarget__StopForTarget__Stop.state == FINISHED && DriveToTarget__StopForTarget__SetDriveFlag.state == WAITING || DriveToTarget__StopForTarget__SetDriveFlag.state == FINISHED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTarget____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&((STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.FINISHED).isTrue()))) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTarget : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTarget__Stop.state == WAITING || DriveToTarget__StopForTarget__Stop.state == FINISHED && DriveToTarget__StopForTarget__SetDriveFlag.state == WAITING || DriveToTarget__StopForTarget__SetDriveFlag.state == FINISHED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                        changeOccurred();
                    } else {
                        if ((STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__Stop().equalTo(NodeState.FINISHED).isTrue())&&(STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.WAITING).isTrue()||STATE___DriveToTarget__StopForTarget__SetDriveFlag().equalTo(NodeState.FINISHED).isTrue())) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTarget : FAILING (3) -> ITERATION_ENDED
<ALL_CHILDREN_WAITING_OR_FINISHED T?> (DriveToTarget__StopForTarget__Stop.state == WAITING || DriveToTarget__StopForTarget__Stop.state == FINISHED && DriveToTarget__StopForTarget__SetDriveFlag.state == WAITING || DriveToTarget__StopForTarget__SetDriveFlag.state == FINISHED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTarget__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTarget : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget.state == WAITING)
[ Reset variables: .outcome, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTarget____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____outcome);
                    DriveToTarget__StopForTarget____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget____failure);
                    DriveToTarget__StopForTarget__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTarget__state.getNext()) {
            case  1 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
            case  2 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
            case  3 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
            case  4 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
            case  5 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
            case  6 :
                MicroStep___DriveToTarget__StopForTarget__SetDriveFlag();
                MicroStep___DriveToTarget__StopForTarget__Stop();
                break;
        }
    }

    public NodeState STATE___DriveToTarget__StopForTarget() {
        switch (DriveToTarget__StopForTarget__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTarget");
    }

    void MicroStep___DriveToTarget__StopForTarget__Stop() {
        switch (DriveToTarget__StopForTarget__Stop__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget__StopForTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2e8f01b8 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget__StopForTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTarget__Stop__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2eb9eee9 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTarget__Stop : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@30f41af6 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                        DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                        DriveToTarget__StopForTarget__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__SetDriveFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTarget__Stop : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTarget__Stop.state && FINISHED == DriveToTarget__StopForTarget__SetDriveFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6a162595 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                            DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                            DriveToTarget__StopForTarget__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTarget__Stop : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : WAITING (6) -> EXECUTING");
                            }
                            getWorld().command(new CommandHandle(DriveToTarget__StopForTarget__Stop____command_handle), StringValue.get(("rover_stop")));
                            endMacroStep();
                            DriveToTarget__StopForTarget__Stop__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@53fd8f9c ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTarget__Stop : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2bdf75d8 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                        DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                        DriveToTarget__StopForTarget__Stop__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTarget__Stop : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (true || .command_handle == COMMAND_DENIED || .command_handle == COMMAND_FAILED)
 ----> (State #3)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : EXECUTING (5) -> FINISHING");
                        }
                        DriveToTarget__StopForTarget__Stop__state.setNext(3);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5bb1ce68 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__StopForTarget__Stop : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@76663cdf ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                        DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                        DriveToTarget__StopForTarget__Stop__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    } else {
                        if (BooleanValue.get(DriveToTarget__StopForTarget__Stop____command_handle.getCurrent().isKnown()).isTrue()) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__StopForTarget__Stop : FINISHING (5) -> ITERATION_ENDED
<COMMAND_ACCEPTED T?> (isKnown(.command_handle))
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@32269133 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                            DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                            DriveToTarget__StopForTarget__Stop__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@206c4fe ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@a6f00bc ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                        DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                        DriveToTarget__StopForTarget__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__SetDriveFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTarget__Stop.state && FINISHED == DriveToTarget__StopForTarget__SetDriveFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__Stop__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTarget__Stop____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&DriveToTarget__StopForTarget__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__Stop__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTarget__Stop____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&DriveToTarget__StopForTarget__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTarget__Stop : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__Stop : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__Stop__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                        changeOccurred();
                    } else {
                        if (DriveToTarget__StopForTarget__Stop____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTarget__Stop : FAILING (3) -> ITERATION_ENDED
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__Stop : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__StopForTarget__Stop__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTarget__Stop : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget__StopForTarget.state == WAITING)
[ Reset variables: .outcome, .command_handle, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__Stop : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTarget__Stop____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____outcome);
                    DriveToTarget__StopForTarget__Stop____command_handle.setNext(CommandHandleState.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____command_handle);
                    DriveToTarget__StopForTarget__Stop____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop____failure);
                    DriveToTarget__StopForTarget__Stop__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__Stop__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTarget__Stop__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__StopForTarget__Stop() {
        switch (DriveToTarget__StopForTarget__Stop__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTarget__Stop");
    }

    void MicroStep___DriveToTarget__StopForTarget__SetDriveFlag() {
        switch (DriveToTarget__StopForTarget__SetDriveFlag__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget__StopForTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@7d993bfd ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                    DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget__StopForTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@57319f76 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                    DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@7adc28b2 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                        DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__SetDriveFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTarget__Stop.state && FINISHED == DriveToTarget__StopForTarget__SetDriveFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@bf3fd22 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                            DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                            DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                            changeOccurred();
                        } else {
                            /*
(State #1) priority 6 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : WAITING (6) -> EXECUTING
<START_CONDITION T?> (true)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : WAITING (6) -> EXECUTING");
                            }
                            if (JavaPlan.DEBUG) {
                                System.out.println(("drive_done: New value set to "+ BooleanValue.get((true))));
                            }
                            DriveToTarget__drive_done.setNext(BooleanValue.get((true)));
                            commitAfterMicroStep(DriveToTarget__drive_done);
                            DriveToTarget__StopForTarget__SetDriveFlag____previous_value.setNext(DriveToTarget__drive_done.getCurrent());
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____previous_value);
                            endMacroStep();
                            DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(2);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3f6fc94f ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                    DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                    if (JavaPlan.DEBUG) {
                        System.out.println(("drive_done: New value set to "+ DriveToTarget__StopForTarget__SetDriveFlag____previous_value.getCurrent()));
                    }
                    DriveToTarget__drive_done.setNext(DriveToTarget__StopForTarget__SetDriveFlag____previous_value.getCurrent());
                    commitAfterMicroStep(DriveToTarget__drive_done);
                    endMacroStep();
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@44098212 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                        DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                        if (JavaPlan.DEBUG) {
                            System.out.println(("drive_done: New value set to "+ DriveToTarget__StopForTarget__SetDriveFlag____previous_value.getCurrent()));
                        }
                        DriveToTarget__drive_done.setNext(DriveToTarget__StopForTarget__SetDriveFlag____previous_value.getCurrent());
                        commitAfterMicroStep(DriveToTarget__drive_done);
                        endMacroStep();
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (5) -> ITERATION_ENDED
<END_CONDITION T?> (true)
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4c3a8ea3 ]
 ----> (State #4)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : EXECUTING (5) -> ITERATION_ENDED");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.SUCCESS);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                        DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(4);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@186a966e ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                    DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@7dba1c47 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                        DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    } else {
                        if ((NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__Stop()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget__SetDriveFlag()).isTrue())||(((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue())) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__StopForTarget__Stop.state && FINISHED == DriveToTarget__StopForTarget__SetDriveFlag.state || FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__StopForTarget__SetDriveFlag____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__StopForTarget__SetDriveFlag____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    } else {
                        /*
(State #5) priority 3 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : FAILING (3) -> ITERATION_ENDED
 ----> (State #4)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : FAILING (3) -> ITERATION_ENDED");
                        }
                        DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(4);
                        commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                        changeOccurred();
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget__StopForTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__StopForTarget__SetDriveFlag : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget__StopForTarget.state == WAITING)
[ Reset variables: .outcome, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__StopForTarget__SetDriveFlag : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__StopForTarget__SetDriveFlag____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____outcome);
                    DriveToTarget__StopForTarget__SetDriveFlag____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag____failure);
                    DriveToTarget__StopForTarget__SetDriveFlag__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__StopForTarget__SetDriveFlag__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__StopForTarget__SetDriveFlag__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__StopForTarget__SetDriveFlag() {
        switch (DriveToTarget__StopForTarget__SetDriveFlag__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__StopForTarget__SetDriveFlag");
    }

    void MicroStep___DriveToTarget__TakeNavcam() {
        switch (DriveToTarget__TakeNavcam__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__TakeNavcam : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4596b9cd ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__TakeNavcam : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__TakeNavcam__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__TakeNavcam : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2a80656c ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__TakeNavcam : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5382ab40 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                        DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                        DriveToTarget__TakeNavcam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__TakeNavcam : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@27b92195 ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakeNavcam : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                            DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                            DriveToTarget__TakeNavcam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                            changeOccurred();
                        } else {
                            if (DriveToTarget__drive_done.getCurrent().isTrue()) {
                                /*
(State #1) priority 5 ----> 
DriveToTarget__TakeNavcam : WAITING (5) -> FINISHED
<SKIP_CONDITION T?> (drive_done)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6b4bd192 ]
 ----> (State #6)
*/
                                if (JavaPlan.DEBUG) {
                                    System.out.println("DriveToTarget__TakeNavcam : WAITING (5) -> FINISHED");
                                }
                                DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SKIPPED);
                                commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                                DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                                commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                                DriveToTarget__TakeNavcam__state.setNext(6);
                                commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                                changeOccurred();
                            } else {
                                if (DriveToTarget__timeout.getCurrent().isTrue()) {
                                    /*
(State #1) priority 6 ----> 
DriveToTarget__TakeNavcam : WAITING (6) -> EXECUTING
<START_CONDITION T?> (timeout)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                                    if (JavaPlan.DEBUG) {
                                        System.out.println("DriveToTarget__TakeNavcam : WAITING (6) -> EXECUTING");
                                    }
                                    getWorld().command(new CommandHandle(DriveToTarget__TakeNavcam____command_handle), StringValue.get(("take_navcam")));
                                    endMacroStep();
                                    DriveToTarget__TakeNavcam__state.setNext(2);
                                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                                    changeOccurred();
                                }
                            }
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__TakeNavcam : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@3eabff46 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__TakeNavcam : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@e046ccf ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                        DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                        DriveToTarget__TakeNavcam__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__TakeNavcam : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (true || .command_handle == COMMAND_DENIED || .command_handle == COMMAND_FAILED)
 ----> (State #3)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : EXECUTING (5) -> FINISHING");
                        }
                        DriveToTarget__TakeNavcam__state.setNext(3);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__TakeNavcam : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@44839139 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__TakeNavcam : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@19b35853 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                        DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                        DriveToTarget__TakeNavcam__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    } else {
                        if (BooleanValue.get(DriveToTarget__TakeNavcam____command_handle.getCurrent().isKnown()).isTrue()) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__TakeNavcam : FINISHING (5) -> ITERATION_ENDED
<COMMAND_ACCEPTED T?> (isKnown(.command_handle))
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4d6bbe53 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakeNavcam : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                            DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                            DriveToTarget__TakeNavcam__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__TakeNavcam : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@5c9c62da ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__TakeNavcam : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1f1e05b6 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                        DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                        DriveToTarget__TakeNavcam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__TakeNavcam : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakeNavcam : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__TakeNavcam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__TakeNavcam : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakeNavcam : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__TakeNavcam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__TakeNavcam____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&DriveToTarget__TakeNavcam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__TakeNavcam : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__TakeNavcam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__TakeNavcam____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&DriveToTarget__TakeNavcam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__TakeNavcam : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakeNavcam : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__TakeNavcam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                        changeOccurred();
                    } else {
                        if (DriveToTarget__TakeNavcam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__TakeNavcam : FAILING (3) -> ITERATION_ENDED
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakeNavcam : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__TakeNavcam__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__TakeNavcam : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget.state == WAITING)
[ Reset variables: .outcome, .command_handle, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakeNavcam : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__TakeNavcam____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____outcome);
                    DriveToTarget__TakeNavcam____command_handle.setNext(CommandHandleState.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____command_handle);
                    DriveToTarget__TakeNavcam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam____failure);
                    DriveToTarget__TakeNavcam__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__TakeNavcam__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__TakeNavcam__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__TakeNavcam() {
        switch (DriveToTarget__TakeNavcam__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__TakeNavcam");
    }

    void MicroStep___DriveToTarget__TakePancam() {
        switch (DriveToTarget__TakePancam__state.getCurrent()) {
            case  0 :
                if (STATE___DriveToTarget().equalTo(NodeState.FINISHED).isTrue()) {
                    /*
(State #0) priority 1 ----> 
DriveToTarget__TakePancam : INACTIVE (1) -> FINISHED
<PARENT_FINISHED T?> (DriveToTarget.state == FINISHED)
[ edu.umn.crisys.plexil.il.action.CompositeAction@6c08ffaf ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : INACTIVE (1) -> FINISHED");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (STATE___DriveToTarget().equalTo(NodeState.EXECUTING).isTrue()) {
                        /*
(State #0) priority 1 ----> 
DriveToTarget__TakePancam : INACTIVE (1) -> WAITING
<PARENT_EXECUTING T?> (DriveToTarget.state == EXECUTING)
 ----> (State #1)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : INACTIVE (1) -> WAITING");
                        }
                        DriveToTarget__TakePancam__state.setNext(1);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    }
                }
                break;
            case  1 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #1) priority 1 ----> 
DriveToTarget__TakePancam : WAITING (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@42ca7192 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : WAITING (1) -> FINISHED");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SKIPPED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #1) priority 3 ----> 
DriveToTarget__TakePancam : WAITING (3) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@78e64a7f ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : WAITING (3) -> FINISHED");
                        }
                        DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SKIPPED);
                        commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                        DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                        commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                        DriveToTarget__TakePancam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #1) priority 4 ----> 
DriveToTarget__TakePancam : WAITING (4) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@572515db ]
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakePancam : WAITING (4) -> FINISHED");
                            }
                            DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SKIPPED);
                            commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                            DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                            DriveToTarget__TakePancam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakePancam__state);
                            changeOccurred();
                        } else {
                            if (DriveToTarget__timeout.getCurrent().isTrue()) {
                                /*
(State #1) priority 5 ----> 
DriveToTarget__TakePancam : WAITING (5) -> FINISHED
<SKIP_CONDITION T?> (timeout)
[ edu.umn.crisys.plexil.il.action.CompositeAction@43be098f ]
 ----> (State #6)
*/
                                if (JavaPlan.DEBUG) {
                                    System.out.println("DriveToTarget__TakePancam : WAITING (5) -> FINISHED");
                                }
                                DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SKIPPED);
                                commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                                DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                                commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                                DriveToTarget__TakePancam__state.setNext(6);
                                commitAfterMicroStep(DriveToTarget__TakePancam__state);
                                changeOccurred();
                            } else {
                                if (DriveToTarget__drive_done.getCurrent().isTrue()) {
                                    /*
(State #1) priority 6 ----> 
DriveToTarget__TakePancam : WAITING (6) -> EXECUTING
<START_CONDITION T?> (drive_done)
<PRE_CONDITION T?> (true)
 ----> (State #2)
*/
                                    if (JavaPlan.DEBUG) {
                                        System.out.println("DriveToTarget__TakePancam : WAITING (6) -> EXECUTING");
                                    }
                                    getWorld().command(new CommandHandle(DriveToTarget__TakePancam____command_handle), StringValue.get(("take_pancam")));
                                    endMacroStep();
                                    DriveToTarget__TakePancam__state.setNext(2);
                                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                                    changeOccurred();
                                }
                            }
                        }
                    }
                }
                break;
            case  2 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #2) priority 1 ----> 
DriveToTarget__TakePancam : EXECUTING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@7165ae07 ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : EXECUTING (1) -> FAILING");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #2) priority 3 ----> 
DriveToTarget__TakePancam : EXECUTING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4a8487ba ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : EXECUTING (3) -> FAILING");
                        }
                        DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                        DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                        DriveToTarget__TakePancam__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    } else {
                        /*
(State #2) priority 5 ----> 
DriveToTarget__TakePancam : EXECUTING (5) -> FINISHING
<END_CONDITION T?> (true || .command_handle == COMMAND_DENIED || .command_handle == COMMAND_FAILED)
 ----> (State #3)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : EXECUTING (5) -> FINISHING");
                        }
                        DriveToTarget__TakePancam__state.setNext(3);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    }
                }
                break;
            case  3 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #3) priority 1 ----> 
DriveToTarget__TakePancam : FINISHING (1) -> FAILING
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@42eaea8e ]
 ----> (State #5)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : FINISHING (1) -> FAILING");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(5);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #3) priority 3 ----> 
DriveToTarget__TakePancam : FINISHING (3) -> FAILING
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@4ccd32f4 ]
 ----> (State #5)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : FINISHING (3) -> FAILING");
                        }
                        DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                        DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                        DriveToTarget__TakePancam__state.setNext(5);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    } else {
                        if (BooleanValue.get(DriveToTarget__TakePancam____command_handle.getCurrent().isKnown()).isTrue()) {
                            /*
(State #3) priority 5 ----> 
DriveToTarget__TakePancam : FINISHING (5) -> ITERATION_ENDED
<COMMAND_ACCEPTED T?> (isKnown(.command_handle))
<POST_CONDITION T?> (true)
[ edu.umn.crisys.plexil.il.action.CompositeAction@33cc5890 ]
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakePancam : FINISHING (5) -> ITERATION_ENDED");
                            }
                            DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.SUCCESS);
                            commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                            DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                            commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                            DriveToTarget__TakePancam__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__TakePancam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  4 :
                if (getInterface().evalAncestorExit().isTrue()) {
                    /*
(State #4) priority 1 ----> 
DriveToTarget__TakePancam : ITERATION_ENDED (1) -> FINISHED
<ANCESTOR_EXITS_DISJOINED T?> (<root node's ancestor exit condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@2cca7081 ]
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : ITERATION_ENDED (1) -> FINISHED");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.INTERRUPTED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_EXITED);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (getInterface().evalAncestorInvariant().isFalse()) {
                        /*
(State #4) priority 2 ----> 
DriveToTarget__TakePancam : ITERATION_ENDED (2) -> FINISHED
<ANCESTOR_INVARIANTS_CONJOINED F?> (<root node's ancestor invariant condition>)
[ edu.umn.crisys.plexil.il.action.CompositeAction@1ea03c20 ]
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : ITERATION_ENDED (2) -> FINISHED");
                        }
                        DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.FAILURE);
                        commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                        DriveToTarget__TakePancam____failure.setNext(NodeFailureType.PARENT_FAILED);
                        commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                        DriveToTarget__TakePancam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    } else {
                        if (((((NodeState.FINISHED.equalTo(STATE___DriveToTarget__Drive()).isTrue()&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTimeout()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__StopForTarget()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakeNavcam()).isTrue())&&NodeState.FINISHED.equalTo(STATE___DriveToTarget__TakePancam()).isTrue())||getInterface().evalAncestorEnd().isTrue()) {
                            /*
(State #4) priority 3 ----> 
DriveToTarget__TakePancam : ITERATION_ENDED (3) -> FINISHED
<ANCESTOR_ENDS_DISJOINED T?> (FINISHED == DriveToTarget__Drive.state && FINISHED == DriveToTarget__StopForTimeout.state && FINISHED == DriveToTarget__StopForTarget.state && FINISHED == DriveToTarget__TakeNavcam.state && FINISHED == DriveToTarget__TakePancam.state || <root node's ancestor end condition>)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakePancam : ITERATION_ENDED (3) -> FINISHED");
                            }
                            DriveToTarget__TakePancam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakePancam__state);
                            changeOccurred();
                        } else {
                            /*
(State #4) priority 4 ----> 
DriveToTarget__TakePancam : ITERATION_ENDED (4) -> FINISHED
<REPEAT_CONDITION F?> (false)
 ----> (State #6)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakePancam : ITERATION_ENDED (4) -> FINISHED");
                            }
                            DriveToTarget__TakePancam__state.setNext(6);
                            commitAfterMicroStep(DriveToTarget__TakePancam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  5 :
                if (DriveToTarget__TakePancam____failure.getCurrent().equalTo(NodeFailureType.PARENT_EXITED).isTrue()&&DriveToTarget__TakePancam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                    /*
(State #5) priority 1 ----> 
DriveToTarget__TakePancam : FAILING (1) -> FINISHED
<FAILURE_IS_PARENT_EXIT T?> (.failure == PARENT_EXITED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : FAILING (1) -> FINISHED");
                    }
                    DriveToTarget__TakePancam__state.setNext(6);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                } else {
                    if (DriveToTarget__TakePancam____failure.getCurrent().equalTo(NodeFailureType.PARENT_FAILED).isTrue()&&DriveToTarget__TakePancam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                        /*
(State #5) priority 2 ----> 
DriveToTarget__TakePancam : FAILING (2) -> FINISHED
<FAILURE_IS_PARENT_FAIL T?> (.failure == PARENT_FAILED)
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #6)
*/
                        if (JavaPlan.DEBUG) {
                            System.out.println("DriveToTarget__TakePancam : FAILING (2) -> FINISHED");
                        }
                        DriveToTarget__TakePancam__state.setNext(6);
                        commitAfterMicroStep(DriveToTarget__TakePancam__state);
                        changeOccurred();
                    } else {
                        if (DriveToTarget__TakePancam____command_handle.getCurrent().equalTo(CommandHandleState.COMMAND_ABORTED).isTrue()) {
                            /*
(State #5) priority 3 ----> 
DriveToTarget__TakePancam : FAILING (3) -> ITERATION_ENDED
<COMMAND_ABORT_COMPLETE T?> (.command_handle == COMMAND_ABORTED)
 ----> (State #4)
*/
                            if (JavaPlan.DEBUG) {
                                System.out.println("DriveToTarget__TakePancam : FAILING (3) -> ITERATION_ENDED");
                            }
                            DriveToTarget__TakePancam__state.setNext(4);
                            commitAfterMicroStep(DriveToTarget__TakePancam__state);
                            changeOccurred();
                        }
                    }
                }
                break;
            case  6 :
                if (STATE___DriveToTarget().equalTo(NodeState.WAITING).isTrue()) {
                    /*
(State #6) priority 1 ----> 
DriveToTarget__TakePancam : FINISHED (1) -> INACTIVE
<PARENT_WAITING T?> (DriveToTarget.state == WAITING)
[ Reset variables: .outcome, .command_handle, .failure, ]
 ----> (State #0)
*/
                    if (JavaPlan.DEBUG) {
                        System.out.println("DriveToTarget__TakePancam : FINISHED (1) -> INACTIVE");
                    }
                    DriveToTarget__TakePancam____outcome.setNext(NodeOutcome.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakePancam____outcome);
                    DriveToTarget__TakePancam____command_handle.setNext(CommandHandleState.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakePancam____command_handle);
                    DriveToTarget__TakePancam____failure.setNext(NodeFailureType.UNKNOWN);
                    commitAfterMicroStep(DriveToTarget__TakePancam____failure);
                    DriveToTarget__TakePancam__state.setNext(0);
                    commitAfterMicroStep(DriveToTarget__TakePancam__state);
                    changeOccurred();
                }
                break;
        }
        /* In Actions executed here: */
        switch (DriveToTarget__TakePancam__state.getNext()) {
        }
    }

    public NodeState STATE___DriveToTarget__TakePancam() {
        switch (DriveToTarget__TakePancam__state.getCurrent()) {
            case  1 :
                return NodeState.WAITING;
            case  2 :
                return NodeState.EXECUTING;
            case  3 :
                return NodeState.FINISHING;
            case  0 :
                return NodeState.INACTIVE;
            case  6 :
                return NodeState.FINISHED;
            case  4 :
                return NodeState.ITERATION_ENDED;
            case  5 :
                return NodeState.FAILING;
        }
        throw new RuntimeException("No state mapping found for DriveToTarget__TakePancam");
    }

    public void doMicroStep() {
        MicroStep___DriveToTarget();
        notifyMicroStep();
    }

    public NodeOutcome getRootNodeOutcome() {
        return DriveToTarget____outcome.getCurrent();
    }

    public NodeState getRootNodeState() {
        return STATE___DriveToTarget();
    }

    public PlanState getSnapshot() {
        PlanState DriveToTarget = new PlanState("DriveToTarget");
        DriveToTarget.addVariable(".state", STATE___DriveToTarget());
        DriveToTarget.addVariable(".outcome", DriveToTarget____outcome.getCurrent());
        DriveToTarget.addVariable("drive_done", DriveToTarget__drive_done.getCurrent());
        DriveToTarget.addVariable(".failure", DriveToTarget____failure.getCurrent());
        DriveToTarget.addVariable("timeout", DriveToTarget__timeout.getCurrent());
        PlanState DriveToTarget__Drive = new PlanState("Drive", DriveToTarget);
        DriveToTarget__Drive.addVariable(".state", STATE___DriveToTarget__Drive());
        DriveToTarget__Drive.addVariable(".outcome", DriveToTarget__Drive____outcome.getCurrent());
        DriveToTarget__Drive.addVariable(".command_handle", DriveToTarget__Drive____command_handle.getCurrent());
        DriveToTarget__Drive.addVariable(".failure", DriveToTarget__Drive____failure.getCurrent());
        DriveToTarget.addChild(DriveToTarget__Drive);
        PlanState DriveToTarget__StopForTimeout = new PlanState("StopForTimeout", DriveToTarget);
        DriveToTarget__StopForTimeout.addVariable(".state", STATE___DriveToTarget__StopForTimeout());
        DriveToTarget__StopForTimeout.addVariable(".outcome", DriveToTarget__StopForTimeout____outcome.getCurrent());
        DriveToTarget__StopForTimeout.addVariable(".failure", DriveToTarget__StopForTimeout____failure.getCurrent());
        PlanState DriveToTarget__StopForTimeout__Stop = new PlanState("Stop", DriveToTarget__StopForTimeout);
        DriveToTarget__StopForTimeout__Stop.addVariable(".state", STATE___DriveToTarget__StopForTimeout__Stop());
        DriveToTarget__StopForTimeout__Stop.addVariable(".outcome", DriveToTarget__StopForTimeout__Stop____outcome.getCurrent());
        DriveToTarget__StopForTimeout__Stop.addVariable(".command_handle", DriveToTarget__StopForTimeout__Stop____command_handle.getCurrent());
        DriveToTarget__StopForTimeout__Stop.addVariable(".failure", DriveToTarget__StopForTimeout__Stop____failure.getCurrent());
        DriveToTarget__StopForTimeout.addChild(DriveToTarget__StopForTimeout__Stop);
        PlanState DriveToTarget__StopForTimeout__SetTimeoutFlag = new PlanState("SetTimeoutFlag", DriveToTarget__StopForTimeout);
        DriveToTarget__StopForTimeout__SetTimeoutFlag.addVariable(".state", STATE___DriveToTarget__StopForTimeout__SetTimeoutFlag());
        DriveToTarget__StopForTimeout__SetTimeoutFlag.addVariable(".outcome", DriveToTarget__StopForTimeout__SetTimeoutFlag____outcome.getCurrent());
        DriveToTarget__StopForTimeout__SetTimeoutFlag.addVariable(".previous_value", DriveToTarget__StopForTimeout__SetTimeoutFlag____previous_value.getCurrent());
        DriveToTarget__StopForTimeout__SetTimeoutFlag.addVariable(".failure", DriveToTarget__StopForTimeout__SetTimeoutFlag____failure.getCurrent());
        DriveToTarget__StopForTimeout.addChild(DriveToTarget__StopForTimeout__SetTimeoutFlag);
        DriveToTarget.addChild(DriveToTarget__StopForTimeout);
        PlanState DriveToTarget__StopForTarget = new PlanState("StopForTarget", DriveToTarget);
        DriveToTarget__StopForTarget.addVariable(".state", STATE___DriveToTarget__StopForTarget());
        DriveToTarget__StopForTarget.addVariable(".outcome", DriveToTarget__StopForTarget____outcome.getCurrent());
        DriveToTarget__StopForTarget.addVariable(".failure", DriveToTarget__StopForTarget____failure.getCurrent());
        PlanState DriveToTarget__StopForTarget__Stop = new PlanState("Stop", DriveToTarget__StopForTarget);
        DriveToTarget__StopForTarget__Stop.addVariable(".state", STATE___DriveToTarget__StopForTarget__Stop());
        DriveToTarget__StopForTarget__Stop.addVariable(".outcome", DriveToTarget__StopForTarget__Stop____outcome.getCurrent());
        DriveToTarget__StopForTarget__Stop.addVariable(".command_handle", DriveToTarget__StopForTarget__Stop____command_handle.getCurrent());
        DriveToTarget__StopForTarget__Stop.addVariable(".failure", DriveToTarget__StopForTarget__Stop____failure.getCurrent());
        DriveToTarget__StopForTarget.addChild(DriveToTarget__StopForTarget__Stop);
        PlanState DriveToTarget__StopForTarget__SetDriveFlag = new PlanState("SetDriveFlag", DriveToTarget__StopForTarget);
        DriveToTarget__StopForTarget__SetDriveFlag.addVariable(".state", STATE___DriveToTarget__StopForTarget__SetDriveFlag());
        DriveToTarget__StopForTarget__SetDriveFlag.addVariable(".outcome", DriveToTarget__StopForTarget__SetDriveFlag____outcome.getCurrent());
        DriveToTarget__StopForTarget__SetDriveFlag.addVariable(".previous_value", DriveToTarget__StopForTarget__SetDriveFlag____previous_value.getCurrent());
        DriveToTarget__StopForTarget__SetDriveFlag.addVariable(".failure", DriveToTarget__StopForTarget__SetDriveFlag____failure.getCurrent());
        DriveToTarget__StopForTarget.addChild(DriveToTarget__StopForTarget__SetDriveFlag);
        DriveToTarget.addChild(DriveToTarget__StopForTarget);
        PlanState DriveToTarget__TakeNavcam = new PlanState("TakeNavcam", DriveToTarget);
        DriveToTarget__TakeNavcam.addVariable(".state", STATE___DriveToTarget__TakeNavcam());
        DriveToTarget__TakeNavcam.addVariable(".outcome", DriveToTarget__TakeNavcam____outcome.getCurrent());
        DriveToTarget__TakeNavcam.addVariable(".command_handle", DriveToTarget__TakeNavcam____command_handle.getCurrent());
        DriveToTarget__TakeNavcam.addVariable(".failure", DriveToTarget__TakeNavcam____failure.getCurrent());
        DriveToTarget.addChild(DriveToTarget__TakeNavcam);
        PlanState DriveToTarget__TakePancam = new PlanState("TakePancam", DriveToTarget);
        DriveToTarget__TakePancam.addVariable(".state", STATE___DriveToTarget__TakePancam());
        DriveToTarget__TakePancam.addVariable(".outcome", DriveToTarget__TakePancam____outcome.getCurrent());
        DriveToTarget__TakePancam.addVariable(".command_handle", DriveToTarget__TakePancam____command_handle.getCurrent());
        DriveToTarget__TakePancam.addVariable(".failure", DriveToTarget__TakePancam____failure.getCurrent());
        DriveToTarget.addChild(DriveToTarget__TakePancam);
        return DriveToTarget;
    }

}
