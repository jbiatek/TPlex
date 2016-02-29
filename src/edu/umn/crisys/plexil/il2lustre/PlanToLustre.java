package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jkind.lustre.ArrayType;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.EnumType;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.IntExpr;
import jkind.lustre.LustreUtil;
import jkind.lustre.NamedType;
import jkind.lustre.Program;
import jkind.lustre.Type;
import jkind.lustre.TypeDef;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.VarDecl;
import jkind.lustre.builders.NodeBuilder;
import jkind.lustre.builders.ProgramBuilder;
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.ast.globaldecl.LookupDecl;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.il.expr.GetNodeStateExpr;
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.ILOperator;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.NamedCondition;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.util.NameUtils;

public class PlanToLustre {

	private static Expr equal(Expr one, Expr two) {
		return new BinaryExpr(one, BinaryOp.EQUAL, two);
	}
	
	public static enum Obligation {
		NONE, EXECUTE
	}
	
	private Plan p;
	
	private ProgramBuilder pb;
	private NodeBuilder nb;
	
	private ReverseTranslationMap reverseMap = new ReverseTranslationMap();
	private ILExprToLustre ilToLustre = new ILExprToLustre(reverseMap);
	private LustrePropertyGenerator properties;
	
	
	public PlanToLustre(Plan p) {
		this.p = p;
	}
	
	public Plan getILPlan() {
		return p;
	}
	
	public String toLustreAsString() {
		return toLustreAsString(Obligation.NONE);
	}
	
	public String toLustreAsString(Obligation obligation) {
		Program program = toLustre(obligation);
		PrettyPrintVisitor pp = new PrettyPrintVisitor();
		program.accept(pp);
		return pp.toString();
	}

	public Program toLustre() {
		return toLustre(Obligation.NONE);
	}
	
	public Program toLustre(Obligation obilgations) {
		pb = getProgramWithPlexilTypes();
		nb = new NodeBuilder(NameUtils.clean(p.getPlanName()));
		properties = new LustrePropertyGenerator(this, nb);
		
		uninlineNamedExpressions();
		
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
		Set<String> allExpectedStrings = getTranslationMap().getAllExpectedStringIds();
		if (! allExpectedStrings.isEmpty()) {
			EnumType planStrings = new EnumType(LustreNamingConventions.STRING_ENUM_NAME, 
				new ArrayList<String>(allExpectedStrings));
			addEnumType(pb, planStrings);
		}
		
		switch(obilgations) {
		case NONE: break;
		case EXECUTE: 
			p.getMachines().stream()
			.flatMap(m -> m.getNodeIds().stream())
			.forEach(uid -> {
				properties.addPlainExecuteProperty(uid);

			});
			break;
		}
		
		// Set node outputs to the things that IL plans expose
		nb.addOutput(new VarDecl("outcome", LustreNamingConventions.POUTCOME));
		nb.addEquation(new Equation(new IdExpr("outcome"), 
				new BinaryExpr(
						toLustre(NodeOutcome.UNKNOWN, ILType.OUTCOME),
						BinaryOp.ARROW,
						toLustre(p.getRootNodeOutcome(), ILType.OUTCOME))));
		
		nb.addOutput(new VarDecl("state", LustreNamingConventions.PSTATE));
		nb.addEquation(new Equation(new IdExpr("state"),
				new BinaryExpr(
						toLustre(NodeState.INACTIVE, ILType.STATE),
						BinaryOp.ARROW,
						toLustre(p.getRootNodeState(), ILType.STATE))));
		
		pb.addNode(nb.build());
		
		Program program = pb.build();
		//StaticAnalyzer.check(program, SolverOption.Z3);
		return program;
	}
	
	public LustrePropertyGenerator getPropertyGenerator() {
		return properties;
	}
	
	
	private void uninlineNamedExpressions() {
		Set<NamedCondition> alreadyDone = new HashSet<>();
		// Read all named expressions and create variables for them. 
		p.modifyAllGuards(new ILExprModifier<Void>() {

			@Override
			public ILExpr visit(NamedCondition named, Void param) {
				if (alreadyDone.contains(named)) {
					return named;
				}
				// Get proper name
				String id = LustreNamingConventions.getNamedConditionId(named);
				// Add as a local variable
				nb.addLocal(new VarDecl(id, LustreNamingConventions.PBOOLEAN));
				// Translate the equation
				nb.addEquation(new Equation(new IdExpr(id), 
						toLustre(named.getExpression(), ILType.BOOLEAN)));
				// Don't repeat for this one again
				alreadyDone.add(named);
				// We don't actually want to change the IL expression though. 
				return super.visit(named, param);
			}
			
		}, null);
		
		
	}

	/**
	 * Add this command handle as a Lustre input. A constrained version will
	 * be created with the proper name as given by LustreNamingConventions.getVariableId(),
	 * which should be used in any normal circumstance. 
	 * 
	 * @param handle The actual IL representation of the handle
	 * @return the raw, unconstrained input ID. 
	 */
	public String addCommandHandleInputFor(ILVariable handle, String cmdName) {
		// In the Lustre translation, command handles are actually pure inputs,
		// not variables. 
		String rawIdName = LustreNamingConventions.getRawCommandHandleId(handle);
		nb.addInput(new VarDecl(rawIdName, LustreNamingConventions.PCOMMAND));
		Expr rawId = id(rawIdName);
		
		String constrainedIdName = LustreNamingConventions.getVariableId(handle);
		nb.addLocal(new VarDecl(constrainedIdName, LustreNamingConventions.PCOMMAND));

		// But this input needs to have restrictions. In addition to the general
		// PLEXIL rule that inputs can't change mid-macrostep, we also want
		// to make sure that it appears to reset, and that it doesn't do weird
		// things like change before the command is even issued.
		
		// First, we'll handle resets. In INACTIVE and WAITING, the only valid
		// value is UNKNOWN. The only way to get to those two states is the 
		// initial state of the plan and by resetting. 
		NodeUID node = handle.getNodeUID();
		
		ILExpr inactive = ILOperator.DIRECT_COMPARE.expr(NodeState.INACTIVE, new GetNodeStateExpr(node));
		ILExpr waiting = ILOperator.DIRECT_COMPARE.expr(NodeState.WAITING, new GetNodeStateExpr(node));
		// If the node was SKIPPED, it must have gone directly from WAITING to
		// FINISHED, with the command not being issued. Therefore, it should 
		// be UNKNOWN too.
		ILExpr skipped = ILOperator.DIRECT_COMPARE.expr(
				NodeOutcome.SKIPPED, 
				getNodeOutcomeFor(node));
		// This is also true if the pre-condition failed.
		ILExpr preFail = ILOperator.DIRECT_COMPARE.expr(
				NodeFailureType.PRE_CONDITION_FAILED,
				getFailureTypeFor(node));
		ILExpr handleShouldBeUntouchedIL = ILOperator.OR.expr(
				inactive, waiting, skipped, preFail);
		
		Expr handleShouldBeUntouched = toLustre(handleShouldBeUntouchedIL);
		Expr cmdUnknown = toLustre(CommandHandleState.UNKNOWN, 
				ILType.COMMAND_HANDLE);
		
		// Build these guards into an equation. 
		Equation e = new Equation(id(constrainedIdName), 
				// Starts out UNKNOWN,
				arrow(cmdUnknown,
				// First and foremost, regardless of the macro step state, if
				// it's untouched it must be UNKNOWN.
				ite(handleShouldBeUntouched, cmdUnknown,
						// Otherwise, if it's the macrostep end, take a new raw
						// value, if not stay the same. 
						ite(isCurrentlyEndOfMacroStep(),
								rawId,
								pre(id(constrainedIdName))))));
		nb.addEquation(e);
		
		reverseMap.addCommandHandleMapping(rawIdName, cmdName);
		 
		return rawIdName;
	}

	private void addLookupToNode(LookupDecl lookup) {
		if (lookup.getParameters().size() > 0) {
			System.err.println("Warning: Lookup parameters are not "
					+ "supported in Lustre translation: "+lookup.getName());
		}
		
		ILType ilType = lookup.getReturnValue().get().getType().toILType();
		Type type = getLustreType(ilType);
		String lookupName = lookup.getName();
		
		if (LustreNamingConventions.hasValueAndKnownSplit(ilType)) {
			String constrainedValue = LustreNamingConventions.getLookupIdValuePart(lookupName);
			String rawValue = LustreNamingConventions.getRawLookupIdValuePart(lookupName);
			String constrainedKnown = LustreNamingConventions.getLookupIdKnownPart(lookupName);
			String rawKnown = LustreNamingConventions.getRawLookupIdKnownPart(lookupName);
			
			// The raw values will be inputs. The constrained values will be
			// equations that enforce PLEXIL semantics. (Lustre asserts caused
			// us a bunch of problems, JKind uses them but slices
			// out their values when returning test cases). 
			nb.addInput(new VarDecl(rawValue, type));
			nb.addInput(new VarDecl(rawKnown, NamedType.BOOL));
			nb.addLocal(new VarDecl(constrainedValue, type));
			nb.addLocal(new VarDecl(constrainedKnown, NamedType.BOOL));
			
			
			// It is only allowed to change between macro steps. Otherwise,
			// lookups are completely unconstrained. 
			PlexilEquationBuilder builderValue = new PlexilEquationBuilder(constrainedValue, id(rawValue));
			builderValue.addAssignmentBetweenMacro(LustreUtil.TRUE, id(rawValue));
			nb.addEquation(builderValue.buildEquation());
			
			PlexilEquationBuilder builderKnown = new PlexilEquationBuilder(constrainedKnown, id(rawKnown));
			builderKnown.addAssignmentBetweenMacro(LustreUtil.TRUE, id(rawKnown));
			nb.addEquation(builderKnown.buildEquation());
			
			// Write this down for reverse mapping later
			reverseMap.addLookupMapping(rawValue, lookup.getName());

		} else {
			String constrained = LustreNamingConventions.getLookupId(lookupName);
			String raw = LustreNamingConventions.getRawLookupId(lookupName);

			// The raw value will be an input. The constrained value will be
			// an equation that enforces PLEXIL semantics. 
			nb.addInput(new VarDecl(raw, type));
			nb.addLocal(new VarDecl(constrained, type));
			
			// It is only allowed to change between macro steps. Otherwise,
			// lookups are completely unconstrained. 
			PlexilEquationBuilder peb = new PlexilEquationBuilder(constrained, id(raw));
			peb.addAssignmentBetweenMacro(LustreUtil.TRUE, id(raw));
			nb.addEquation(peb.buildEquation());
			
			// Write this down for reverse mapping later
			reverseMap.addLookupMapping(raw, lookup.getName());
		}
	}
	
	public Expr toLustre(ILExpr e, ILType expectedType) {
		return e.accept(ilToLustre, expectedType);
	}
	
	public Expr toLustre(ILExpr e) {
		return e.accept(ilToLustre, ILType.NATIVE_BOOL);
	}
	
	public ReverseTranslationMap getTranslationMap() {
		return reverseMap;
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
				ite(or(equal(id("first"), id("p_false")),
						equal(id("second"), id("p_false"))), id("p_false"),
				ite(or(equal(id("first"), id("p_unknown")),
					equal(id("second"), id("p_unknown"))), id("p_unknown"),
				id("p_true")))
				));
		pb.addNode(p_and.build());
		
		NodeBuilder p_or = new NodeBuilder(LustreNamingConventions.OR_OPERATOR);
		p_or.addInput(new VarDecl("first", LustreNamingConventions.PBOOLEAN));
		p_or.addInput(new VarDecl("second", LustreNamingConventions.PBOOLEAN));
		p_or.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_or.addEquation(new Equation(new IdExpr("result"), 
				ite(or(equal(id("first"), id("p_true")),
						equal(id("second"), id("p_true"))), id("p_true"),
				ite(or(equal(id("first"), id("p_unknown")),
					equal(id("second"), id("p_unknown"))), id("p_unknown"),
				id("p_false")))
				));
		pb.addNode(p_or.build());
		
		

		NodeBuilder p_not = new NodeBuilder(LustreNamingConventions.NOT_OPERATOR);
		p_not.addInput(new VarDecl("value", LustreNamingConventions.PBOOLEAN));
		p_not.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_not.addEquation(new Equation(new IdExpr("result"), 
				ite(equal(id("value"), id("p_true")), id("p_false"),
				ite(equal(id("value"), id("p_false")), id("p_true"),
						id("p_unknown")))));
		pb.addNode(p_not.build());
		
		NodeBuilder p_eq_bool = new NodeBuilder(LustreNamingConventions.EQ_BOOL_OPERATOR);
		p_eq_bool.addInput(new VarDecl("first", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addInput(new VarDecl("second", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		p_eq_bool.addEquation(new Equation(new IdExpr("result"), 
				ite(or(equal(id("first"), id("p_unknown")), 
                       equal(id("second"), id("p_unknown"))), id("p_unknown"),
                   ite(equal(id("first"), id("second")), id("p_true"), id("p_false")))));
		pb.addNode(p_eq_bool.build());
		

		NodeBuilder to_pboolean = new NodeBuilder(LustreNamingConventions.TO_PBOOLEAN_OPERATOR);
		to_pboolean.addInput(new VarDecl("value", NamedType.BOOL));
		to_pboolean.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		to_pboolean.addEquation(new Equation(new IdExpr("result"), 
				ite(id("value"), id("p_true"), id("p_false"))));
		pb.addNode(to_pboolean.build());
		
		// Numeric operations that use the unknown bit
		Map<String, BinaryOp> boolComparators = new HashMap<>();
		boolComparators.put("ge", BinaryOp.GREATEREQUAL);
		boolComparators.put("gt", BinaryOp.GREATER);
		boolComparators.put("le", BinaryOp.LESSEQUAL);
		boolComparators.put("lt", BinaryOp.LESS);
		boolComparators.put("eq", BinaryOp.EQUAL);
		for (Entry<String, BinaryOp> e : boolComparators.entrySet()) {
			// Do an integer version and a real version
			pb.addNode(numericComparator("pint_"+e.getKey(), e.getValue(), NamedType.INT));
			pb.addNode(numericComparator("preal_"+e.getKey(), e.getValue(), NamedType.REAL));
		}
		
		// Since tuples aren't really first-class citizens, we unfortunately
		// can't declare nodes that take tuples and therefore can't have 
		// those operations as nodes. Instead, they're basically inlined. 
		// Expressions use the value component and the known component 
		// separately. We do, however, need to implement a few "native"
		// operations. 
		
		
		
		return pb;
	}
	
	private static jkind.lustre.Node numericComparator(String name, BinaryOp op, Type t) {
		NodeBuilder nb = new NodeBuilder(name);
		nb.addInput(new VarDecl("av", t));
		nb.addInput(new VarDecl("ak", NamedType.BOOL));
		nb.addInput(new VarDecl("bv", t));
		nb.addInput(new VarDecl("bk", NamedType.BOOL));
		nb.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		nb.addEquation(new Equation(id("result"), 
				ite(and(id("ak"), id("bk")), 
						ite(new BinaryExpr(id("av"), op, id("bv")),
							LustreNamingConventions.P_TRUE,
							LustreNamingConventions.P_FALSE),
						LustreNamingConventions.P_UNKNOWN)
				));
		
		
		return nb.build();
	}
	
	private static void addEnumType(ProgramBuilder pb, EnumType et) {
		pb.addType(new TypeDef(et.id, et));
	}
	
	private static Type getLustreType(ILType t) {
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
	
	private static Type getLustreType(ILType arrayType, int size) {
		return new ArrayType(getLustreType(arrayType.elementType()), size);
	}
	
	
	private void addVariableToNode(ILVariable v) {
		// Assignments are handled by actions. We'll do the declaration 
		// and leave the rest to them. 
		Type t;
		Type knownFlagType = NamedType.BOOL;
		if (v instanceof ArrayVar) {
			ArrayVar array = (ArrayVar) v;
			t = getLustreType(array.getType(), array.getMaxSize());
			knownFlagType = new ArrayType(NamedType.BOOL, array.getMaxSize());
		} else {
			t = getLustreType(v.getType());
		}
		
		if (v.getType() == ILType.COMMAND_HANDLE) {
			// Really, this is an input. We need the command's information 
			// too, so the Command action is responsible for telling us to
			// add this variable. 
			return;
		}
		if (v.getType().isNumeric() 
				|| (v.getType().isArrayType() 
						&& v.getType().elementType().isNumeric())	) {
			// Declare both the value and the known bit.
			nb.addLocal(new VarDecl(LustreNamingConventions
					.getNumericVariableValueId(v), t));
			nb.addLocal(new VarDecl(LustreNamingConventions
					.getNumericVariableKnownId(v), knownFlagType));
		} else {
			// Everything else is a simple enum, including an UNKNOWN entry
			nb.addLocal(new VarDecl(LustreNamingConventions.getVariableId(v), t));
		}
	}

	private static IdExpr getStateExpr(NodeStateMachine nsm) {
		return new IdExpr(LustreNamingConventions.getStateId(nsm));
	}
	
	public static Expr getPlexilStateExprForNode(NodeUID uid) {
		return id(LustreNamingConventions.getStateMapperId(uid));
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
			Expr map = ilToLustre.visit(NodeState.INACTIVE, ILType.STATE);
			for (State s : nsm.getStates()) {
				map = new IfThenElseExpr(
						// if the real state variable is this one
						new BinaryExpr(getStateExpr(nsm), BinaryOp.EQUAL, 
								new IntExpr(s.getIndex())),
						// then return the tag
						toLustre(s.tags.get(uid), ILType.STATE),
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
				getStateExpr(nsm), new IntExpr(0));
		
		// Make sure we're doing this in order.
		nsm.orderTransitionsByPriority();
		
		// Add guards for each transition with results
		for (Transition t : nsm.getTransitions()) {
			state.addAssignment(createGuardWithStateCheck(getStateExpr(nsm), t),
					new IntExpr(t.end.getIndex()));
		}
		
		// That should take care of it! Just build this equation.  
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
		// Guards look at values as they existed at the end of the last step
		return pre(toLustre(t.guard));
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
		Expr specifiedState = new IntExpr(s.getIndex());
		return new BinaryExpr(
				new BinaryExpr(previousState, BinaryOp.NOTEQUAL, specifiedState), 
				BinaryOp.AND, 
				new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState));
	}
	
	public static Expr getCurrentlyInSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState = getStateExpr(nsm);
		Expr specifiedState = new IntExpr(s.getIndex());
		return new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState);
	}
	
	public static Expr getStateIsUnchanged(NodeStateMachine nsm) {
		return new BinaryExpr(getStateExpr(nsm), 
				BinaryOp.EQUAL, 
				new UnaryExpr(UnaryOp.PRE, getStateExpr(nsm)));
	}
	
	public static Expr isCurrentlyEndOfMacroStep() {
		return LustreNamingConventions.MACRO_STEP_ENDED;
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
				new IntExpr(t.start.getIndex()));
		
		// Then the transition's guards have to apply, if any.
		if (t.isAlwaysTaken()) {
			// Oh, it's always taken. Well we're done then. 
			return guardExpr;
		}
		return new BinaryExpr(guardExpr, BinaryOp.AND, getGuardExprFor(t));

	}
	
	public ILVariable getNodeOutcomeFor(NodeUID uid) {
		return p.getVariables().stream()
			.filter(v -> v.getNodeUID().equals(uid) 
					&& v.getType().equals(ILType.OUTCOME))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Outcome for "+uid+" not found!"));
	}
	
	public ILVariable getFailureTypeFor(NodeUID uid) {
		return p.getVariables().stream()
				.filter(v -> v.getNodeUID().equals(uid) 
						&& v.getType().equals(ILType.FAILURE))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Failure for "+uid+" not found!"));
	}

}
