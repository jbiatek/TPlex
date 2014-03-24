package edu.umn.crisys.plexil.java.values;

public interface PValueVisitor<Param, Return> {

	public Return visitBooleanValue(BooleanValue bool, Param param);
	public Return visitIntegerValue(IntegerValue integer, Param param);
	public Return visitRealValue(RealValue real, Param param);
	public Return visitStringValue(StringValue string, Param param);
	public Return visitUnknownValue(UnknownValue unk, Param param);
	public Return visitPValueList(PValueList<?> list, Param param);
	public Return visitCommandHandleState(CommandHandleState state, Param param);
	public Return visitNodeFailure(NodeFailureType type, Param param);
	public Return visitNodeOutcome(NodeOutcome outcome, Param param);
	public Return visitNodeState(NodeState state, Param param);
}
