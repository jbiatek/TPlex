package edu.umn.crisys.plexil.test.java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles the PLEXIL regression test suite the same way the official Perl 
 * scripts do, except that we are testing ourselves against the executive
 * instead of comparing to a "blessed" previous run.  
 * 
 * The "parser error" tests are skipped, since any errors there are Not Our 
 * Problem (TM). 
 * 
 * @author jbiatek
 *
 */
public class OfficialNASARegressionTests {

	/**
	 * These are all defined as: 
	 * 
	 * "Tests requiring no external interaction, for which root node success 
	 * is sufficient".
	 * 
	 * In other words, none of these should depend on the environment in any
	 * way, no lookups or commands or anything, so just give them an empty
	 * script as an external world. The root node should eventually progress
	 * to FINISHED, with an outcome of SUCCESS, at which point the test has
	 * passed. 
	 * 
	 */
	public static final Set<String> EMPTY_SCRIPT_TESTS = new HashSet<>();
	static { 
		EMPTY_SCRIPT_TESTS.addAll(Arrays.asList(
				"AncestorReferenceTest", 
				"array2", 
				"array5", 
				"array6", 
				"array9", 
				"ArrayInLoop", 
				"ArrayAssignmentWithFailure", 
				"AssignmentFailureTest", 
				"AssignFailureWithConflict", 
				"concat1", 
				"contention1", 
				"contention3", 
				"DoubleInvariantAssignment", 
				"empty1", 
				"empty2", 
				"empty3", 
				"empty4", 
				"EmptyString1", 
				"FailureType1", 
				"FailureType2", 
				"FailureType3", 
				"FailureType4", 
				"GrandparentAccess", 
				"interface1", 
				"IterationEnded1", 
				"invariant1", 
				"isKnown1", 
				"maxTest", 
				"minTest", 
				"modulo1", 
				"repeat1", 
				"repeat3", 
				"repeat4", 
				"SimpleAssignment", 
				"skip1", 
				"skip2", 
				"TestAbsSqrt", 
				"TestNodeNameScope", 
				"TestNodeNameScopeHack", 
				"TestRepeatCondition", 
				"TimepointVariableConstructionOrder", 
				"UninitializedAssignment", 
				"var-priority-with-exit", 
				"var-priority-with-skip", 
				"variables1", 
				"whitespace1"));
	}
	
	/**
	 * These are all defined as:
	 * 
	 *  "Tests requiring no external interaction, whose execution traces are 
	 *  compared against a "gold standard""
	 *  
	 *  Unlike the other tests, these might or might not "run to success". 
	 *  
	 *  In our case, the PLEXIL executive is the gold standard, so compare 
	 *  against that. 
	 *  
	 *  (What they do is write log files into a "valid" directory,
	 *  and manually inspect them for errors. Then to run the regression test 
	 *  they compare the output, and if it has changed then the test has 
	 *  failed.)
	 */
	public static final Set<String> EMPTY_SCRIPT_VALID_TESTS = new HashSet<>();
	static {
		EMPTY_SCRIPT_VALID_TESTS.addAll(Arrays.asList(
				"AssignToParentInvariant", "AssignToParentExit",
				"InactiveAncestorInvariantTest", "NonLocalExit"));
	}
	
	
	/**
	 * This category isn't found in the PLEXIL test scripts, but it should
	 * be there. These are scripts that include libraries. The map points
	 * the main plan to the libraries it uses. 
	 */
	public static final Map<String,Set<String>> EMPTY_SCRIPT_LIBRARY_TESTS = new HashMap<>();
	static {
		EMPTY_SCRIPT_LIBRARY_TESTS.put("LibraryCallWithArray", 
				new HashSet<>(Arrays.asList("LibraryNodeWithArray")));
	}
	
	/**
	 * This category isn't found in the PLEXIL test scripts, but it should
	 * be there. These are scripts that include libraries. The map points
	 * the main plan to the libraries it uses. 
	 */
	public static final Map<String,Set<String>> SAME_NAME_LIBRARY_TESTS = new HashMap<>();
	static {
		SAME_NAME_LIBRARY_TESTS.put("AssignmentMain", 
				new HashSet<>(Arrays.asList("DoValue", "GetValue")));
	}


	/**
	 * These are all defined as:
	 * 
	 *  "Tests which run to success and require a script of the same name"
	 * 
	 * This means that there should be both "NAME.plx" and "NAME.psx", where
	 * the PSX file will provide an environment for the script to run in. 
	 * These should otherwise follow the "EMPTY_SCRIPT_TESTS" guidelines, 
	 * where eventually the root node will go to FINISHED/SUCCESS, which counts
	 * as a success. 
	 */
	public static final Set<String> SAME_NAME_SCRIPT_TESTS = new HashSet<>();
	static {
		SAME_NAME_SCRIPT_TESTS.addAll(Arrays.asList(
				"array1", 
				"array3", 
				"array4", 
				"array8", 
				"AtomicAssignment", 
				"boolean1", 
				"ChangeLookupTest", 
				"command1", 
				"command2", 
				"command3", 
				"command4", 
				"command5", 
				"concat2", 
				"conjuncts", 
				"conjuncts1", 
				"lookup1", 
				"lookup2", 
				"lookup3", 
				"repeat2", 
				"repeat5", 
				"repeat7", 
				"repeat8", 
				"SiteSurveyWithEOF", 
				"TestEndCondition", 
				"TestTimepoint", 
				"UpdateLookupTest", 
				"UpdateTest"
				// "AssignmentMain" is in this list, but it actually uses
				// libraries, so I moved it. 
				));
	}

	/**
	 * These are all defined as:
	 * 
	 * "Resource arbitration tests, which require comparing the output with a 
	 * known standard"
	 * 
	 * TPlex doesn't support resources, so ignore all of these. If you do want
	 * to support them, they should each have a "same name" PSX file, and they
	 * should be compared to the "gold standard", not "run to success". 
	 */
	public static final Set<String> RESOURCE_ARBITRATION_TESTS = new HashSet<>();
	static {
		RESOURCE_ARBITRATION_TESTS.addAll(Arrays.asList(
				"resource1", 
				"Resource1RepeatCond", 
				"Resource2EqualPriority", 
				"Resource3AckRel", 
				"Resource3DenyHP", 
				"Resource3Deny2HP", 
				"Resource4Hvm", 
				"Resource4HvmRepeatCond", 
				"ResourceRenewable1", 
				"NonUnaryResources"
				));
	}
	
	/**
	 * These two scripts are both run against SimpleDrive.plx. 
	 * 
	 * They are also compared to a "gold standard".  
	 */
	public static final Set<String> SIMPLE_DRIVE_SCRIPTS = new HashSet<>();
	static {
		SIMPLE_DRIVE_SCRIPTS.add("single-drive");
		SIMPLE_DRIVE_SCRIPTS.add("double-drive");
	}
	
	/**
	 * These are tests that simply don't work for various reasons.
	 */
	public static final Set<String> BLACKLIST = new HashSet<>();
	static {
		// According to the comment in this file, it's supposed to be invalid?
		// It certainly looks invalid. But the executive actually takes it and 
		// runs it anyway? I don't understand. Go away. 
		BLACKLIST.add("interface1");

		// This legitimately seems to have an error in it, with a noderef
		// to "Three_Min_Timer" which is very clearly not there. It does
		// specifiy a direction of "self", which might mean that it's supposed
		// to ignore the name? If so, that's dumb. Deal with this later.
		BLACKLIST.add("TestTimepoint");
		
		// This one depends on implementing command aborts. 
		BLACKLIST.add("SiteSurveyWithEOF");
		
		// This one depends on being able to pass entire arrays through
		// library node calls. I added AST support for doing this, and it
		// goes into the IL with no errors, but that's all.
		BLACKLIST.add("LibraryCallWithArray");
		
		// We don't support resource arbitration at all.
		BLACKLIST.addAll(RESOURCE_ARBITRATION_TESTS);
	}
	
	/**
	 * These are tests that don't work in Lustre for various reasons.
	 */
	public static final Set<String> LUSTRE_BLACKLIST = new HashSet<>();
	static {
		// Anything that doesn't work in the IL certainly won't in Lustre.
		LUSTRE_BLACKLIST.addAll(BLACKLIST);
		
		// Parameterized lookups aren't supported yet.
		LUSTRE_BLACKLIST.add("SimpleDrive");
		LUSTRE_BLACKLIST.add("SimpleDriveRegressionTest");
		
		// Array assignments aren't supported yet.
		LUSTRE_BLACKLIST.add("AssignmentFailureTest");
		LUSTRE_BLACKLIST.add("ArrayAssignmentWithFailure");
		LUSTRE_BLACKLIST.add("ArrayInLoop");
		LUSTRE_BLACKLIST.add("array4");
		LUSTRE_BLACKLIST.add("array5");
		
		// Returning values from commands isn't supported yet.
		LUSTRE_BLACKLIST.add("command3");
		LUSTRE_BLACKLIST.add("array1");
		LUSTRE_BLACKLIST.add("array3");
		
		// String concatenation is probably never going to be supported.
		LUSTRE_BLACKLIST.add("concat1");
		LUSTRE_BLACKLIST.add("concat2");
		
		
		// Same for dynamic command names and lookups.
		LUSTRE_BLACKLIST.add("command1");
		LUSTRE_BLACKLIST.add("command2");
		LUSTRE_BLACKLIST.add("command4");
		LUSTRE_BLACKLIST.add("lookup2");
	}

}
