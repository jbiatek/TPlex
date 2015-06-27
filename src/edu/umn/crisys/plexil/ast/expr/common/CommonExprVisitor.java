package edu.umn.crisys.plexil.ast.expr.common;

import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PValueList;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;


public interface CommonExprVisitor<Param, Return> {
    
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

	
    public Return visitArrayIndex(ArrayIndexExpr array, Param param);
    
    public Return visitLookupNow(LookupNowExpr lookup, Param param);
    
    public Return visitLookupOnChange(LookupOnChangeExpr lookup, Param param);
    
    public Return visitOperation(Operation op, Param param);
    
}
