package edu.umn.crisys.plexil.il2lustre;

import static jkind.lustre.LustreUtil.id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jkind.lustre.EnumType;
import jkind.lustre.IdExpr;
import jkind.lustre.TupleExpr;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.expr.ExprType;
import edu.umn.crisys.plexil.expr.Expression;
import edu.umn.crisys.plexil.expr.NamedCondition;
import edu.umn.crisys.plexil.expr.common.LookupExpr;
import edu.umn.crisys.plexil.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.expr.il.vars.ILVariable;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.runtime.values.BooleanValue;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.IntegerValue;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.PValue;
import edu.umn.crisys.plexil.runtime.values.RealValue;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;

public class LustreNamingConventions {
	public static final String P_TRUE_ID = "p_true";
	public static final String P_FALSE_ID = "p_false";
	public static final String P_UNKNOWN_ID = "p_unknown";
	
	public static final IdExpr P_TRUE = new IdExpr(P_TRUE_ID);
	public static final IdExpr P_FALSE = new IdExpr(P_FALSE_ID);
	public static final IdExpr P_UNKNOWN = new IdExpr(P_UNKNOWN_ID);
	
	public static final boolean KNOWN_FLAG = true;
	public static final boolean UNKNOWN_FLAG = !KNOWN_FLAG;
	
	public static final String STRING_ENUM_NAME = "PlexilString";
	public static final String UNKNOWN_STRING = "unknown_str__";
	public static final String EMPTY_STRING = "empty_str__";
	
	public static final EnumType PBOOLEAN = new EnumType("pboolean", 
			Arrays.asList(P_UNKNOWN_ID, P_TRUE_ID, P_FALSE_ID)); 
	
	public static final EnumType PSTATE = new EnumType("pstate", 
			enumerator(NodeState.values()));
	
	public static final EnumType PCOMMAND = new EnumType("command_handle", 
			enumerator(CommandHandleState.values()));

	public static final EnumType POUTCOME = new EnumType("node_outcome", 
			enumerator(NodeOutcome.values()));
	
	public static final EnumType PFAILURE = new EnumType("node_failure", 
			enumerator(NodeFailureType.values()));
	
	/**
	 * Indicates whether this micro step is the last one of the macro step. 
	 * After this variable becomes true, ALL state machines and actions should
	 * maintain their current values until it becomes false again (it should 
	 * only be one step). The only exception is events which occur between
	 * macro steps, such as command handle or variable returns. 
	 */
	public static String MACRO_STEP_ENDED_ID = "macrostep_end";
	public static IdExpr MACRO_STEP_ENDED = new IdExpr(MACRO_STEP_ENDED_ID);
	
	public static final String AND_OPERATOR = "p_and";
	public static final String OR_OPERATOR = "p_or";
	public static final String NOT_OPERATOR = "p_not";
	public static final String EQ_BOOL_OPERATOR = "p_eq_boolean";
	public static final String TO_PBOOLEAN_OPERATOR = "to_pboolean";
	public static final String XOR_OPERATOR = "p_xor";

	private static List<String> enumerator(Enum<?>[] values) {
		List<String> ret = new ArrayList<String>();
		for (Enum<?> e : values) {
			ret.add(getEnumId(e));
		}
		return ret;
	}
	
	public static String getEnumId(Enum<?> value) {
		if (value.name().equalsIgnoreCase("unknown")) {
			return value.getClass().getSimpleName().toString().toLowerCase()
					+"_"+value.name().toLowerCase();
		}
		return value.name().toLowerCase();
	}

	public static PValue reverseTranslate(String enumValue, 
			Optional<ReverseTranslationMap> stringMap) {
		if (enumValue.equals(P_TRUE_ID)) return BooleanValue.get(true);
		if (enumValue.equals(P_FALSE_ID)) return BooleanValue.get(false);
		if (enumValue.equals(P_UNKNOWN_ID)) return UnknownValue.get();
		if (enumValue.equals(UNKNOWN_STRING)) return UnknownValue.get();
		if (enumValue.equals(EMPTY_STRING)) return StringValue.get("");
		
		for (NodeState state : NodeState.values()) {
			if (getEnumId(state).equals(enumValue)) return state;
		}
		for (NodeOutcome outcome : NodeOutcome.values()){ 
			if (getEnumId(outcome).equals(enumValue)) return outcome;
		}
		for (NodeFailureType failure : NodeFailureType.values()){ 
			if (getEnumId(failure).equals(enumValue)) return failure;
		}
		for (CommandHandleState cmdHandle : CommandHandleState.values()){ 
			if (getEnumId(cmdHandle).equals(enumValue)) return cmdHandle;
		}
		if (enumValue.matches("\\d+")) {
			return IntegerValue.get(Integer.parseInt(enumValue));
		} else if (enumValue.matches("\\d*\\.\\d*")) {
			return RealValue.get(Double.parseDouble(enumValue));
		}
		
		if (stringMap.isPresent()) {
			Optional<PString> ret = stringMap.get().getPStringFromEnumId(enumValue);
			return ret.orElseThrow(() -> new RuntimeException
					("String "+enumValue+" not found in map"));
		}
		throw new RuntimeException("Couldn't reverse translate "+enumValue+", "
				+ "there was no string map so it might be a string.");
	}
	
	public static PValue reverseTranslateNumber(String valuePart, 
			String knownPart, ExprType type) {
		if (knownPart.equals("false")) {
			return UnknownValue.get();
		}
		if (type == ExprType.INTEGER) {
			return IntegerValue.get(Integer.parseInt(valuePart));
		}
		if (type == ExprType.REAL) {
			return RealValue.get(Double.parseDouble(valuePart));
		}
		throw new RuntimeException("Type "+type+" is not a number.");
	}
	
	private static String getLookupId(Expression rawName) {
		//TODO: Lookup parameters, right now only one value per lookup name
		
		if (rawName instanceof StringValue) {
			StringValue name = (StringValue) rawName;
			return getLookupId(name.asString());
		}
		// I don't think we're ever going to be able to support non-constant
		// lookup names in Lustre.
		throw new RuntimeException(rawName+" is dynamic.");
	}
	
	public static String getLookupId(LookupExpr lookupExpr) {
		return getLookupId(lookupExpr.getLookupNameAsString());
	}
	
	public static String getLookupIdKnownPart(LookupExpr lookupExpr) {
		return getLookupIdKnownPart(lookupExpr.getLookupNameAsString());
	}

	public static String getLookupIdValuePart(LookupExpr lookupExpr) {
		return getLookupIdValuePart(lookupExpr.getLookupNameAsString());
	}
	
	public static String getLookupId(String lookupName) {
		return NameUtils.clean("Lookup/"+lookupName);
	}
	
	public static String getLookupIdValuePart(String lookupName) {
		return NameUtils.clean("Lookup/"+lookupName+".value");
	}
	
	public static String getLookupIdKnownPart(String lookupName) {
		return NameUtils.clean("Lookup/"+lookupName+".isknown");
	}
	
	public static boolean hasValueAndKnownSplit(ILVariable v) {
		return hasValueAndKnownSplit(v.getType());
	}
	
	public static boolean hasValueAndKnownSplit(ExprType type) {
		if (type == ExprType.UNKNOWN) {
			throw new RuntimeException("Types need to be known to answer this");
		}
		return type == ExprType.INTEGER
				|| type == ExprType.REAL
				|| type == ExprType.INTEGER_ARRAY
				|| type == ExprType.REAL_ARRAY;
	}

	public static String getVariableId(ILVariable v) {
		if (hasValueAndKnownSplit(v)) {
			throw new RuntimeException("Numeric variables have a value "
					+ "component and a 'known' component: "+v);
		}
		return NameUtils.clean(v.getNodeUID() + "/" + v.getName());
	}
	
	public static jkind.lustre.Expr getNumericValue(ILVariable v) {
		return new TupleExpr(Arrays.asList(id(getNumericVariableValueId(v)),
				id(getNumericVariableKnownId(v))));
	}
	
	public static String getNumericVariableValueId(ILVariable v) {
		if ( ! hasValueAndKnownSplit(v)) {
			throw new RuntimeException("Not a numeric variable: "+v);
		}
		
		return NameUtils.clean(v.getNodeUID() + "/" + v.getName()+".value");
	}
	
	public static String getNumericVariableKnownId(ILVariable v) {
		return NameUtils.clean(v.getNodeUID() + "/" + v.getName()+".isknown");
	}

	public static String getStateId(NodeStateMachine nsm) {
		return NameUtils.clean(nsm.getStateMachineId());
	}

	public static String getStateMapperId(NodeUID uid) {
		return NameUtils.clean(uid.toString()+"__state");
	}
	
	public static String getNamedConditionId(NamedCondition named) {
		return NameUtils.clean(named.getUid()+"/"
				+named.getDescription().toString().toLowerCase());
	}

	
}
