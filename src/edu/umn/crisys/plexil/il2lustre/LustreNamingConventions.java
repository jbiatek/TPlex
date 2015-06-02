package edu.umn.crisys.plexil.il2lustre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jkind.lustre.EnumType;
import jkind.lustre.IdExpr;
import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.ast.expr.Expression;
import edu.umn.crisys.plexil.ast.expr.common.LookupNowExpr;
import edu.umn.crisys.plexil.ast.expr.common.LookupOnChangeExpr;
import edu.umn.crisys.plexil.il.NodeUID;
import edu.umn.crisys.plexil.il.statemachine.NodeStateMachine;
import edu.umn.crisys.plexil.il.vars.ILVariable;
import edu.umn.crisys.plexil.runtime.values.CommandHandleState;
import edu.umn.crisys.plexil.runtime.values.NodeFailureType;
import edu.umn.crisys.plexil.runtime.values.NodeOutcome;
import edu.umn.crisys.plexil.runtime.values.NodeState;
import edu.umn.crisys.plexil.runtime.values.StringValue;

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
	public static final String UNKNOWN_STRING = "_unknown_str_";
	public static final String EMPTY_STRING = "_empty_str_";
	
	public static final EnumType PBOOLEAN = new EnumType("pboolean", 
			Arrays.asList(P_TRUE_ID, P_FALSE_ID, P_UNKNOWN_ID)); 
	
	public static final EnumType PSTATE = new EnumType("pstate", 
			enumerator(NodeState.values()));
	
	public static final EnumType PCOMMAND = new EnumType("command_handle", 
			enumerator(CommandHandleState.values()));

	public static final EnumType POUTCOME = new EnumType("node_outcome", 
			enumerator(NodeOutcome.values()));
	
	public static final EnumType PFAILURE = new EnumType("node_failure", 
			enumerator(NodeFailureType.values()));
	
	public static String MACRO_STEP_ENDED_ID = "macrostep_end";
	
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
	
	public static String getLookupId(String lookupName) {
		return NameUtils.clean("Lookup/"+lookupName);
	}
	
	public static String getRawLookupId(String lookupName) {
		return getLookupId(lookupName+"/raw");
	}
	
	public static String getInputName(LookupNowExpr lookup) {
		return getLookupId(lookup.getLookupName());
	}
	
	public static String getInputName(LookupOnChangeExpr lookup) {
		return getLookupId(lookup.getLookupName());
	}
	
	public static String getVariableId(ILVariable v) {
		return NameUtils.clean(v.getNodeUID() + "/" + v.getName());
	}

	public static String getStateId(NodeStateMachine nsm) {
		return NameUtils.clean(nsm.getStateMachineId());
	}

	public static String getStateMapperId(NodeUID uid) {
		return NameUtils.clean(uid.toString()+"__state");
	}

}