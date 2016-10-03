package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jkind.SolverOption;
import jkind.lustre.ArrayType;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.CastExpr;
import jkind.lustre.EnumType;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.IntExpr;
import jkind.lustre.LustreUtil;
import jkind.lustre.NamedType;
import jkind.lustre.Program;
import jkind.lustre.RealExpr;
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
import edu.umn.crisys.plexil.il.expr.ILExpr;
import edu.umn.crisys.plexil.il.expr.ILExprModifier;
import edu.umn.crisys.plexil.il.expr.ILType;
import edu.umn.crisys.plexil.il.expr.NamedCondition;
import edu.umn.crisys.plexil.il.expr.vars.ArrayVar;
import edu.umn.crisys.plexil.il.expr.vars.ILVariable;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.statemachine.State;
import edu.umn.crisys.plexil.il.statemachine.Transition;
import edu.umn.crisys.plexil.jkind.search.JKindSettings;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.StringValue;
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
	
	public void addGenericStrings(int numStrings) {
		for (int i=0; i < numStrings; i++) {
			toLustre(StringValue.get("String"+i));
		}
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
			// Now that we have the full enum type, we need to create the 
			// equality operator. 
			pb.addNode(createPEqualNode(ILType.STRING, planStrings));
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
		JKindSettings.staticCheckLustreProgram(program, SolverOption.Z3);
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
	 * Add a "raw" input variable for this ILVariable. The ID(s) that are added
	 * can be obtained with the LustreNamingConventions class.
	 * 
	 * @param v
	 */
	public void addRawInputFor(ILVariable v) {
		List<VarDecl> inputs = new ArrayList<>();
		if (LustreNamingConventions.hasValueAndKnownSplit(v)) {
			inputs.add(new VarDecl(LustreNamingConventions.getRawInputIdValuePart(v), 
					getLustreType(v.getType())));
			inputs.add(new VarDecl(LustreNamingConventions.getRawInputIdKnownPart(v), 
					NamedType.BOOL));
		} else {
			inputs.add(new VarDecl(LustreNamingConventions.getRawInputId(v),
					getLustreType(v.getType())));
		}
		nb.addInputs(inputs);
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
			reverseMap.addLookupMapping(constrainedValue, lookup.getName());

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
			reverseMap.addLookupMapping(constrained, lookup.getName());
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
	
	private ProgramBuilder getProgramWithPlexilTypes() {
		ProgramBuilder pb = new ProgramBuilder();
		// Declare all the special types that we have.
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
		
		// Numeric max and min for ints and reals
		pb.addNode(createMaxOrMin(ILType.INTEGER, true));
		pb.addNode(createMaxOrMin(ILType.INTEGER, false));
		pb.addNode(createMaxOrMin(ILType.REAL, true));
		pb.addNode(createMaxOrMin(ILType.REAL, false));
		
		// Lustre defines "mod" for integers, but not reals.
		
		NodeBuilder real_mod = new NodeBuilder("real_mod");
		// We will follow C++'s "std::fmod()" function, since I'm guessing
		// that is what the interpreter does.
		
		// The floating-point remainder of the division operation x/y 
		// calculated by this function is exactly the value x - n*y, where n 
		// is x/y with its fractional part truncated.
		real_mod.addInput(new VarDecl("x", NamedType.REAL));
		real_mod.addInput(new VarDecl("y", NamedType.REAL));
		real_mod.addOutput(new VarDecl("o", NamedType.REAL));
		Expr n = new CastExpr(NamedType.REAL, 
					new CastExpr(NamedType.INT, 
							new BinaryExpr(id("x"), BinaryOp.DIVIDE, id("y"))));
		real_mod.addEquation(new Equation(id("o"), 
				new BinaryExpr(id("x"), BinaryOp.MINUS, 
						new BinaryExpr(n, BinaryOp.MULTIPLY, id("y")))));
		pb.addNode(real_mod.build());
		
		// Absolute value function needs to be defined.
		NodeBuilder int_abs = new NodeBuilder("int_abs");
		int_abs.addInput(new VarDecl("n", NamedType.INT));
		int_abs.addOutput(new VarDecl("out", NamedType.INT));
		int_abs.addEquation(new Equation(id("out"), 
				ite(new BinaryExpr(id("n"), BinaryOp.LESS, new IntExpr(0)),
						new UnaryExpr(UnaryOp.NEGATIVE, id("n")),
						id("n"))));
		
		NodeBuilder real_abs = new NodeBuilder("real_abs");
		real_abs.addInput(new VarDecl("n", NamedType.REAL));
		real_abs.addOutput(new VarDecl("out", NamedType.REAL));
		real_abs.addEquation(new Equation(id("out"), 
				ite(new BinaryExpr(id("n"), BinaryOp.LESS, new RealExpr(new BigDecimal(0.0))),
						new UnaryExpr(UnaryOp.NEGATIVE, id("n")),
						id("n"))));

		
		// Equality operations for enums that have unknowns
		pb.addNode(createPEqualNode(ILType.BOOLEAN, LustreNamingConventions.PBOOLEAN));
		pb.addNode(createPEqualNode(ILType.OUTCOME, LustreNamingConventions.POUTCOME));
		pb.addNode(createPEqualNode(ILType.FAILURE, LustreNamingConventions.PFAILURE));
		pb.addNode(createPEqualNode(ILType.COMMAND_HANDLE, LustreNamingConventions.PCOMMAND));
		// Strings are also in this category, but we don't know what the strings
		// are yet. When the string enum is added to the plan, then we can 
		// create the p_eq node for them. 
		
		return pb;
	}
	
	private jkind.lustre.Node createMaxOrMin(ILType ilType, boolean doMax) {
		if (ilType != ILType.INTEGER && ilType != ILType.REAL) {
			throw new RuntimeException("No min/max operator for "+ilType);
		}
		String name = (ilType == ILType.INTEGER ? "int" : "real")
						+ "_"
						+ (doMax ? "max" : "min");
		NamedType lustreType = ilType == ILType.INTEGER ? NamedType.INT : NamedType.REAL;
		
		NodeBuilder operator = new NodeBuilder(name);
		operator.addInput(new VarDecl("a", lustreType));
		operator.addInput(new VarDecl("b", lustreType));
		operator.addOutput(new VarDecl("o", lustreType));
		
		BinaryOp comparator;
		if (doMax) {
			comparator = BinaryOp.GREATER;
		} else {
			comparator = BinaryOp.LESS;
		}
		
		operator.addEquation(new Equation(id("o"),
				ite(
						new BinaryExpr(id("a"), comparator, id("b")),
						id("a"),
						id("b"))));
		
		return operator.build();
	}
	
	private jkind.lustre.Node createPEqualNode(ILType ilType, EnumType lustreType) {
		Expr unknown = toLustre(ilType.getUnknown(), ilType);
		
		NodeBuilder eqOperator = new NodeBuilder(LustreNamingConventions
				.getEqualityOperatorName(lustreType));
		eqOperator.addInput(new VarDecl("first", lustreType));
		eqOperator.addInput(new VarDecl("second", lustreType));
		eqOperator.addOutput(new VarDecl("result", LustreNamingConventions.PBOOLEAN));
		eqOperator.addEquation(new Equation(new IdExpr("result"), 
				ite(or(equal(id("first"), unknown), 
                       equal(id("second"), unknown)), LustreNamingConventions.P_UNKNOWN,
                   ite(equal(id("first"), id("second")), id("p_true"), id("p_false")))));
		return eqOperator.build();
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
		if (nsm.getNodeIds().size() == 1) {
			// This machine only has 1 node in it. We can just do the
			// PLEXIL state machine directly, with no integer states at all.
			NodeUID uid = nsm.getNodeIds().get(0);
			String id = LustreNamingConventions.getStateMapperId(uid);
			VarDecl machine = new VarDecl(id, LustreNamingConventions.PSTATE);
			nb.addLocal(machine);
			nb.addEquation(singleNodeStateMachineEquation(nsm));
			// That's it! The state mapper ID is actually the direct machine,
			// but no one else needs to know that.
			return;
		}
		
		
		// Create the integer state machine, and also a state mapper for PLEXIL
		// to read node states from it. 
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
	
	private static boolean isSingleNodeStateMachine(NodeStateMachine nsm) {
		return nsm.getNodeIds().size() == 1;
	}
	
	private static NodeUID getSingleNodeUID(NodeStateMachine nsm) {
		if ( ! isSingleNodeStateMachine(nsm)) {
			throw new RuntimeException("Not a single node state machine, "
					+ "it has "+nsm.getNodeIds().size()+" nodes.");
		}
		return nsm.getNodeIds().get(0);
	}
	
	public Equation singleNodeStateMachineEquation(NodeStateMachine nsm) {
		NodeUID theNode = getSingleNodeUID(nsm);
		IdExpr varName = id(LustreNamingConventions.getStateMapperId(theNode));
		
		// We always start in Inactive.
		PlexilEquationBuilder state = new PlexilEquationBuilder(
				varName, toLustre(NodeState.INACTIVE));
		
		// Make sure transitions are in the right order.
		nsm.orderTransitionsByPriority();
		
		// Add guards and their destination states
		for (Transition t : nsm.getTransitions()) {
			Expr guard = getGuardExprFor(t);
			// Check that we were in the start state before
			Expr stateCheck = equal(toLustre(t.start.tags.get(theNode)), 
					pre(varName));
			state.addAssignment(and(stateCheck, guard), 
					toLustre(t.end.tags.get(theNode)));
		}
		
		// That's all! Build it and move on.
		return state.buildEquation();
	}
	
	public Equation stateMachineEquation(NodeStateMachine nsm) {
		// By IL semantics, we always start in state 0. 
		PlexilEquationBuilder state = new PlexilEquationBuilder(
				getStateExpr(nsm), new IntExpr(0));
		
		// Make sure we're doing this in order.
		nsm.orderTransitionsByPriority();
		
		// Add guards for each transition with results
		for (Transition t : nsm.getTransitions()) {
			state.addAssignment(createGuardWithStateCheck(nsm, t),
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
		Expr specificGuard = createGuardWithStateCheck(nsm, t);
		
		Set<Transition> highers = nsm.getHigherTransitionsThan(t);
		
		for (Transition higherPriority : highers) {
			specificGuard = new BinaryExpr(specificGuard, BinaryOp.AND, 
					new UnaryExpr(UnaryOp.NOT, getGuardExprFor(higherPriority)));
		}
		
		return specificGuard;
	}
	
	public static Expr getEnteringThisSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState;
		Expr specifiedState;
		if (isSingleNodeStateMachine(nsm)) {
			currentState = getPlexilStateExprForNode(getSingleNodeUID(nsm));
			specifiedState = id(
					LustreNamingConventions.getEnumId(
							s.tags.get(getSingleNodeUID(nsm))));
		} else {
			currentState = getStateExpr(nsm);
			specifiedState = new IntExpr(s.getIndex());
		}
		
		return new BinaryExpr(
				new BinaryExpr(pre(currentState), BinaryOp.NOTEQUAL, specifiedState), 
				BinaryOp.AND, 
				new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState));
	}
	
	public static Expr getCurrentlyInSpecificState(NodeStateMachine nsm, State s) {
		Expr currentState;
		Expr specifiedState;
		if (isSingleNodeStateMachine(nsm)) {
			currentState = getPlexilStateExprForNode(getSingleNodeUID(nsm));
			specifiedState = id(
					LustreNamingConventions.getEnumId(
							s.tags.get(getSingleNodeUID(nsm))));
		} else {
			currentState = getStateExpr(nsm);
			specifiedState = new IntExpr(s.getIndex());
		}
		return new BinaryExpr(currentState, BinaryOp.EQUAL, specifiedState);
	}
	
	public static Expr getStateIsUnchanged(NodeStateMachine nsm) {
		Expr currentState;
		if (isSingleNodeStateMachine(nsm)) {
			currentState = getPlexilStateExprForNode(getSingleNodeUID(nsm));
		} else {
			currentState = getStateExpr(nsm);
		}

		return new BinaryExpr(currentState, 
				BinaryOp.EQUAL, 
				pre(currentState));
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
	private Expr createGuardWithStateCheck(NodeStateMachine nsm, Transition t) {
		Expr currentState;
		Expr transitionOrigin;
		
		if (isSingleNodeStateMachine(nsm)) {
			// The state machine is going to use PLEXIL states directly.
			currentState = getPlexilStateExprForNode(getSingleNodeUID(nsm));
			transitionOrigin = toLustre(t.start.tags.get(getSingleNodeUID(nsm)));
		} else {
			currentState = getStateExpr(nsm);
			transitionOrigin = new IntExpr(t.start.getIndex());
		}
		
		
		// First off, we need to have been in the start state.
		Expr guardExpr = new BinaryExpr(
				pre(currentState), 
				BinaryOp.EQUAL, 
				transitionOrigin);
		
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
