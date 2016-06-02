package edu.umn.crisys.plexil.test.java;

import java.util.Arrays;
import java.util.HashSet;
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
	 * These are only defined as "Tests of library calls". 
	 * 
	 * However, the test script just runs it the same as the "empty script tests". 
	 * There doesn't appear to be any special treatment. FYI, the plan here
	 * uses "LibraryNodeWithArray.plx" as its library node. 
	 */
	public static final Set<String> LIBRARY_TESTS = new HashSet<>();
	static {
		LIBRARY_TESTS.add("LibraryCallWithArray");
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
				"UpdateTest", 
				"AssignmentMain"));
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

}
