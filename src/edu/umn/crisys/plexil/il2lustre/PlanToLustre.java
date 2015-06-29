package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import jkind.lustre.ArrayType;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.EnumType;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.NamedType;
import jkind.lustre.Program;
import jkind.lustre.Type;
import jkind.lustre.TypeDef;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.ILExpression;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.nativebool.NativeExpr;
import edu.umn.crisys.plexil.il.expr.nativebool.PlexilExprToNative;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.il.vars.ArrayVar;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class PlanToLustre {

	public static enum Obligation {
		NONE, EXECUTE
	}
	
	private Plan p;
	
	private ProgramBuilder pb = getProgramWithPlexilTypes();
	private NodeBuilder nb;
	
	private ILExprToLustre ilToLustre = new ILExprToLustre();
	private NativeExprToLustre nativeToLustre = new NativeExprToLustre(ilToLustre);

	public PlanToLustre(Plan p) {
		this.p = p;
		
		this.nb = new NodeBuilder(NameUtils.clean(p.getPlanName()));
	}
	
	public Program toLustre() {
		return toLustre(Obligation.NONE);
	}
	
	public Program toLustre(Obligation obilgations) {
		
		
		// Add in declared lookups
		for (LookupDecl lookup : p.getStateDecls()) {
			addLookupToNode(lookup);
		}
		
		for (NodeStateMachine nsm : p.getMachines()) {
			addStateMachineToNode(nsm);
		}
		
		for (ILVariable v : p.getVariables()) {
			addVariableToNode(v);
		}
		
		// Translate actions
		ActionsToLustre a2l = new ActionsToLustre(this);
		a2l.navigate(p);
		a2l.toLustre(nb);
		
		// Add string enum, if any
		Map<String, PString> allExpectedStrings = getStringMap();
		if (! allExpectedStrings.keySet().isEmpty()) {
			EnumType planStrings = new EnumType(LustreNamingConventions.STRING_ENUM_NAME, 
				new ArrayList<String>(allExpectedStrings.keySet()));
			addEnumType(pb, planStrings);
		}
		
		switch(obilgations) {
		case NONE: break;
		case EXECUTE: 
			p.getMachines().stream()
			.flatMap(m -> m.getNodeIds().stream())
			.forEach(uid -> {
				String id = LustreNamingConventions.getStateMapperId(uid)+"_executes";
				nb.addLocal(new VarDecl(id, NamedType.BOOL));
				// This state could never be executing, right? Prove me wrong!
				nb.addEquation(new Equation(new IdExpr(id), 
						new BinaryExpr(
								new IdExpr(LustreNamingConventions.getStateMapperId(uid)), 
								BinaryOp.NOTEQUAL, 
								toLustre(NodeState.EXECUTING, PlexilType.STATE))
						));
				nb.addProperty(id);

			});
			
			
		}
		
		
		pb.addNode(nb.build());
		return pb.build();
	}
	
	
	/**
	 * Adds an input to the NodeBuilder for this command handle. This
	 * is *raw* input, so it is completely unconstrained. ActionsToLustre, the
	 * class responsible for finding and translating Actions like assigning 
	 * variables and issuing commands, is probably the only class that should
	 * use this. (It will create a constrained variable that uses this input
	 * as a source of nondeterminism but adheres to Plexil's semantics.)
	 * @param handle The actual Plexil handle (for naming purposes)
	 * @return the Lustre ID for the new input, which is also available from 
	 * LustreNamingConventions.getRawCommandHandleId(). 
	 */
	public String addRawCommandHandleInputFor(ILVariable handle) {
		// We'll need a raw input of type command handle
		String rawInputId = LustreNamingConventions.getRawCommandHandleId(handle);
		Type cmdType = LustreNamingConventions.PCOMMAND;
		nb.addInput(new VarDecl(rawInputId, cmdType));
		return rawInputId;
	}

	private void addLookupToNode(LookupDecl lookup) {
		if (lookup.getParameters().size() > 0) {
			System.err.println("Warning: Lookup parameters are not "
					+ "supported in Lustre translation: "+lookup.getName());
		}
		
		
		String rawInputId = LustreNamingConventions.getRawLookupId(lookup.getName());
		String lookupId = LustreNamingConventions.getLookupId(lookup.getName());
		Type type = getLustreType(lookup.getReturnValue().get().getType());
		nb.addInput(new VarDecl(rawInputId, type));
		// The "real" lookup can only change between macro steps
		nb.addLocal(new VarDecl(lookupId, type));
		// lookup = raw -> if (last macro step ended) then raw else pre(lookup)
		nb.addEquation(new Equation(new IdExpr(lookupId), 
				new BinaryExpr(new IdExpr(rawInputId), 
						BinaryOp.ARROW, 
						new IfThenElseExpr(
								new UnaryExpr(UnaryOp.PRE, new IdExpr(LustreNamingConventions.MACRO_STEP_ENDED_ID)), 
								new IdExpr(rawInputId), 
								new UnaryExpr(UnaryOp.PRE, new IdExpr(lookupId)))
				)));
	}
	
	public Expr toLustre(ILExpression e, PlexilType expectedType) {
		return e.accept(ilToLustre, expectedType);
	}
	
	public Expr toLustre(NativeExpr e) {
		return e.accept(nativeToLustre, null);
	}
	
	public Map<String,PString> getStringMap() {
		return ilToLustre.getAllExpectedStrings();
	}
	
	private static ProgramBuilder getProgramWithPlexilTypes() {
		ProgramBuilder pb = new ProgramBuilder();
		addEnumType(pb, LustreNamingConventions.PBOOLEAN);
		addEnumType(pb, LustreNamingConventions.PSTATE);
		addEnumType(pb, LustreNamingConventions.PCOMMAND);
		addEnumType(pb, LustreNamingConventions.POUTCOME);
		addEnumType(pb, LustreNamingConventions.PFAILURE);
		
		// Some Plexil operators too
		NodeBuilder p_and = new NodeBuilder(LustreNamingConventions.AND_OPERATOR);
		p_and.addInput(new VarDecl("first", LustreNamingConventions.PBOOLEAN));
		p_and.addInput(new VarDecl("second", LustreNamingConventions.PBOOLEAN));
		p_and.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_and.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_false or second = p_false) then p_false\n"
           +"else if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else p_true")));
		pb.addNode(p_and.build());
		
		NodeBuilder p_or = new NodeBuilder(LustreNamingConventions.OR_OPERATOR);
		p_or.addInput(new VarDecl("first", LustreNamingConventions.PBOOLEAN));
		p_or.addInput(new VarDecl("second", LustreNamingConventions.PBOOLEAN));
		p_or.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_or.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_true or second = p_true) then p_true\n"
           +"else if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else p_false")));
		pb.addNode(p_or.build());
		
		

		NodeBuilder p_not = new NodeBuilder(LustreNamingConventions.NOT_OPERATOR);
		p_not.addInput(new VarDecl("value", LustreNamingConventions.PBOOLEAN));
		p_not.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_not.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if value = p_true then p_false\n"
           +"else if value = p_false then p_true\n"
           +"else p_unknown")));
		pb.addNode(p_not.build());
		
		NodeBuilder p_eq_bool = new NodeBuilder(LustreNamingConventions.EQ_BOOL_OPERATOR);
		p_eq_bool.addInput(new VarDecl("first", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addInput(new VarDecl("second", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if (first = p_unknown or second = p_unknown) then p_unknown\n"
           +"else if (first = second) then p_true\n"
           +"else p_false")));
		pb.addNode(p_eq_bool.build());
		

		NodeBuilder to_pboolean = new NodeBuilder(LustreNamingConventions.TO_PBOOLEAN_OPERATOR);
		to_pboolean.addInput(new VarDecl("value", NamedType.BOOL));
		to_pboolean.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		to_pboolean.addEquation(new Equation(new IdExpr("result"), 
				new IdExpr("if value then p_true else p_false")));
		pb.addNode(to_pboolean.build());
		
		return pb;
	}
	
	private static void addEnumType(ProgramBuilder pb, EnumType et) {
		pb.addType(new TypeDef(et.id, et));
	}
	
	private static Type getLustreType(PlexilType t) {
		switch (t) {
		case BOOLEAN:
			return LustreNamingConventions.PBOOLEAN;
		case INTEGER:
			return NamedType.INT;
		case REAL:
			return NamedType.REAL;
		case STRING:
			return new NamedType(LustreNamingConventions.STRING_ENUM_NAME);
		case STATE:
			return LustreNamingConventions.PSTATE;
		case COMMAND_HANDLE:
			return LustreNamingConventions.PCOMMAND;
		case OUTCOME:
			return LustreNamingConventions.POUTCOME;
		case FAILURE:
			return LustreNamingConventions.PFAILURE;
		case BOOLEAN_ARRAY:
		case INTEGER_ARRAY:
		case REAL_ARRAY:
		case STRING_ARRAY:
		case ARRAY:
			throw new RuntimeException("Array types require a length");
		case UNKNOWN:
		default:
			throw new RuntimeException("Lustre needs everything to be typed!");
		}
	}
	
	private static Type getLustreType(PlexilType arrayType, int size) {
		return new ArrayType(getLustreType(arrayType.elementType()), size);
	}
	
	
	private void addVariableToNode(ILVariable v) {
		// Assignments are handled by actions. We'll do the declaration 
		// and leave the rest to them. 
		Type t;
		if (v instanceof ArrayVar) {
			ArrayVar array = (ArrayVar) v;
			t = getLustreType(array.getType(), array.getMaxSize());
		} else {
			t = getLustreType(v.getType());
		}
		
		nb.addLocal(new VarDecl(LustreNamingConventions.getVariableId(v), t));
	}

	private static IdExpr getStateExpr(NodeStateMachine nsm) {
		return new IdExpr(LustreNamingConventions.getStateId(nsm));
	}
	
	public static Expr getPlexilStateExprForNode(NodeUID uid) {
		return new UnaryExpr(UnaryOp.PRE, new IdExpr(
				LustreNamingConventions.getStateMapperId(uid)));
	}

	private void addStateMachineToNode(NodeStateMachine nsm) {
		String id = LustreNamingConventions.getStateId(nsm);
		
		VarDecl stateVar = new VarDecl(id, NamedType.INT);
		
		nb.addLocal(stateVar);
		nb.addEquation(stateMachineEquation(nsm));
		
		// We also need to map back to Plexil state.
		for (NodeUID uid : nsm.getNodeIds()) {
			VarDecl mapper = new VarDecl(LustreNamingConventions.getStateMapperId(uid), 
					LustreNamingConventions.PSTATE);
			Expr map = ilToLustre.visitNodeState(NodeState.INACTIVE, PlexilType.STATE);
			for (State s : nsm.getStates()) {
				map = new IfThenElseExpr(
						// if the real state variable is this one
						new BinaryExpr(getStateExpr(nsm), BinaryOp.EQUAL, 
								new IdExpr(s.getIndex()+"")),
						// then return the tag
						toLustre(s.tags.get(uid), PlexilType.STATE),
						// else, the rest
						map);
			}
			
			nb.addLocal(mapper);
			nb.addEquation(new Equation(new IdExpr(
					LustreNamingConventions.getStateMapperId(uid)), map));
		}
	}
	
	public Equation stateMachineEquation(NodeStateMachine nsm) {
		// By IL semantics, we always start in state 0. 
		PlexilEquationBuilder state = new PlexilEquationBuilder(
				getStateExpr(nsm), new IdExpr("0"));
		
		// Make sure we're doing this in order.
		nsm.orderTransitionsByPriority();
		
		// Add guards for each transition with results
		for (Transition t : nsm.getTransitions()) {
			state.addAssignment(createGuardWithStateCheck(getStateExpr(nsm), t),
					new IdExpr(t.end.getIndex()+""));
		}
		
		// We have the big if, but we need to initialize. 
		return state.buildEquation();
	}
	
	/**
	 * Get the guard expression for this transition. It doesn't include the 
	 * precondition that the current state is this transition's start state,
	 * and it also doesn't include the precondition that there isn't a higher
	 * priority transition active.  
	 * @param t
	 * @return
	 */
	public Expr getGuardExprFor(Transition t) {
		return toLustre(t.guard);
	}
	
	/**
	 * Creates an expression that is true if and only if the given transition is
	 * active in this step. 
	 * @param t
	 * @param nsm
	 * @param stateId
	 * @return
	 */
	public Expr getGuardForThisSpecificTransition(Transition t, NodeStateMachine nsm) {
		// This transition has to be active, but also all the transitions before 
		// it have to be inactive. 
		Expr specificGuard = createGuardWithStateCheck(getStateExpr(nsm), t);
		
		Set<Transition> highers = nsm.getHigherTransitionsThan(t);
		
		for (Transition higherPriority : highers) {
			specificGuard = new BinaryExpr(specificGuard, BinaryOp.AND, 
					new UnaryExpr(UnaryOp.NOT, getGuardExprFor(higherPriority)));
		}
		
		return specificGuard;
	}
	
	public static Expr getEnteringThisSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState = getStateExpr(nsm);
		Expr previousState = new UnaryExpr(UnaryOp.PRE, currentState);
		Expr specifiedState = new IdExpr(s.getIndex()+"");
		return new BinaryExpr(
				new BinaryExpr(previousState, BinaryOp.NOTEQUAL, specifiedState), 
				BinaryOp.AND, 
				new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState));
	}
	
	public static Expr getCurrentlyInSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState = getStateExpr(nsm);
		Expr specifiedState = new IdExpr(s.getIndex()+"");
		return new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState);
	}
	
	public static Expr getStateIsUnchanged(NodeStateMachine nsm) {
		return new BinaryExpr(getStateExpr(nsm), 
				BinaryOp.EQUAL, 
				new UnaryExpr(UnaryOp.PRE, getStateExpr(nsm)));
	}
	
	public static Expr isInMacrostepWaitingPeriod() {
		return new UnaryExpr(UnaryOp.PRE, LustreNamingConventions.MACRO_STEP_ENDED);
	}
	
	/**
	 * Create a guard that includes a check that the current state is this
	 * transition's origin state. Does NOT include a check for a higher priority
	 * transition. It's assumed that you're checking transitions in order.
	 * @param stateId
	 * @param t
	 * @return
	 */
	public Expr createGuardWithStateCheck(Expr stateId, Transition t) {
		// First off, we need to have been in the start state.
		Expr guardExpr = new BinaryExpr(
				new UnaryExpr(UnaryOp.PRE, stateId), 
				BinaryOp.EQUAL, 
				new IdExpr(t.start.getIndex()+""));
		
		// Then the transition's guards have to apply, if any.
		if (t.isAlwaysTaken()) {
			// Oh, it's always taken. Well we're done then. 
			return guardExpr;
		}
		return new BinaryExpr(guardExpr, BinaryOp.AND, getGuardExprFor(t));

	}

}
