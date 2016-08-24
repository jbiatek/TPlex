package edu.umn.crisys.plexil.jkind.search;

import java.io.PrintStream;
import java.security.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;

import jkind.SolverOption;
import jkind.analysis.StaticAnalyzer;
import jkind.api.JKindApi;
import jkind.api.results.JKindResult;
import jkind.api.results.PropertyResult;
import jkind.api.results.Status;
import jkind.lustre.Program;
import jkind.lustre.VarDecl;
import jkind.lustre.values.Value;
import jkind.results.Counterexample;
import jkind.results.InvalidProperty;
import jkind.results.Signal;
import lustre.LustreTrace;

public class JKindSettings {
	
	public static JKindSettings createBMCOnly(int timeout, int iteration) {
		return new JKindSettings(timeout, iteration, false, false, true, 0);
	}
	
	
	/**
	 * Statically check a Lustre program with JKind's static checker. However,
	 * this method doesn't let JKind do a System.exit(), instead it throws
	 * a runtime exception so you can continue to execute if you want. 
	 * 
	 * @param p
	 * @param opt
	 * @return
	 */
	public static void staticCheckLustreProgram(Program p, SolverOption opt) {
		SysExitStopper stopper = new SysExitStopper();
		stopper.suppressSystemExit();
		try {
			StaticAnalyzer.check(p, opt);
		} catch (edu.umn.crisys.plexil.jkind.search.JKindSettings
				.SysExitStopper.SystemExitSuppressedException se) {
			throw new RuntimeException("Static analysis of Lustre program failed. \n"
					+ "The errors are probably above this message.\n"
					+ "Here is the program that was checked: \n\n"
					+ p, se);
		}
		stopper.returnToNormalOperation();
	}
	
	private static class SysExitStopper {
		
		private SecurityManager old = null;
		
		public static class SystemExitSuppressedException extends SecurityException {
			private static final long serialVersionUID = -3036213945357116554L;

			public SystemExitSuppressedException() {
				super("System.exit() supressed");
			}
		}
		
		public void suppressSystemExit() {
			old = System.getSecurityManager();
			final SecurityManager sm = new SecurityManager() {
				@Override
				public void checkPermission(Permission permission) {
					if (permission.getName().contains("exitVM")) {
						throw new SystemExitSuppressedException();
					}
				}
			};
			System.setSecurityManager(sm);
		}
		
		public void returnToNormalOperation() {
			System.setSecurityManager(old);
		}
	}
	
	/*private final int timeout = 100;
	private final int iteration = 200;
	private final boolean k_induction = true;
	private final boolean invariant_gen = true;
	private final boolean bmc = true;
	private final int pdr_threads = 1;
	*/
	
	private final int timeout;
	private final int iteration;
	private final boolean k_induction;
	private final boolean invariant_gen;
	private final boolean bmc;
	private final int pdr_threads;

	public JKindSettings() {
		this(100, 200, true, true, true, 1);
	}
	
	public JKindSettings(int timeout, int iteration) {
		this(timeout, iteration, true, true, true, 1);
	}
	
	public JKindSettings(int timeout, int iteration, boolean k_induction, 
			boolean invariant_gen, boolean bmc,	int pdr_threads) {
		super();
		this.timeout = timeout;
		this.iteration = iteration;
		this.k_induction = k_induction;
		this.invariant_gen = invariant_gen;
		this.bmc = bmc;
		this.pdr_threads = pdr_threads;
	}

	public Map<String, LustreTrace> execute(Program p) {
		return execute(p, Optional.empty());
	}
	
	public Map<String, LustreTrace> execute(Program p, PrintStream out) {
		return execute(p, Optional.of(out));
	}

	public Map<String, LustreTrace> execute(Program p, Optional<PrintStream> printer) {
		Map<String, LustreTrace> output = new HashMap<String, LustreTrace>();

		JKindResult result = new JKindResult(null);
		NullProgressMonitor monitor = new NullProgressMonitor();

		JKindApi jkind = new JKindApi();
		// Configure it to our specifications
		jkind.setN(iteration);
		jkind.setTimeout(timeout);
		jkind.setSolver(SolverOption.Z3);
		jkind.setPdrMax(pdr_threads);
		if ( ! k_induction ) {
			jkind.disableKInduction();
		}
		if ( ! invariant_gen) {
			jkind.disableInvariantGeneration();
		}
		if ( ! bmc) {
			jkind.disableBoundedModelChecking();
		}

		printer.ifPresent( out -> out.println("------------Executing JKind ..."));
		printer.ifPresent( out -> out.println("Iterations: " + iteration));
		printer.ifPresent( out -> out.println("Timeout: " + timeout + " seconds"));

		staticCheckLustreProgram(p, SolverOption.Z3);
		
		jkind.execute(p, result, monitor);

		printer.ifPresent( out -> out.println("------------JKind checked "
				+ result.getPropertyResults().size() + " properties"));

		for (PropertyResult pr : result.getPropertyResults()) {
			if (pr.getStatus().equals(Status.INVALID)) {
				InvalidProperty ipr = (InvalidProperty) pr.getProperty();
				Counterexample ce = ipr.getCounterexample();

				LustreTrace testCase = generateInputValues(ce, p);
				output.put(pr.getName(), testCase);
			} else {
				output.put(pr.getName(), null);
			}
		}
		return output;

	}
	
	// Generate input values for the program from a counter-example
	// Null values are added if an input variable is "don't care"
	// Each signal may also contain null values.
	public static LustreTrace generateInputValues(Counterexample ce,
			Program program) {
		LustreTrace output = new LustreTrace(ce.getLength());

		List<VarDecl> inputs = program.getMainNode().inputs;

		for (VarDecl input : inputs) {
			Signal<Value> signal = ce.getSignal(input.id);

			// If JKind does not produce values for this variable
			if (signal == null) {
				output.addVariable(new Signal<Value>(input.id));
			} else {
				output.addVariable(signal);
			}
		}

		return output;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bmc ? 1231 : 1237);
		result = prime * result + (invariant_gen ? 1231 : 1237);
		result = prime * result + iteration;
		result = prime * result + (k_induction ? 1231 : 1237);
		result = prime * result + pdr_threads;
		result = prime * result + timeout;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JKindSettings other = (JKindSettings) obj;
		if (bmc != other.bmc)
			return false;
		if (invariant_gen != other.invariant_gen)
			return false;
		if (iteration != other.iteration)
			return false;
		if (k_induction != other.k_induction)
			return false;
		if (pdr_threads != other.pdr_threads)
			return false;
		if (timeout != other.timeout)
			return false;
		return true;
	}

	public int getTimeout() {
		return timeout;
	}
	public int getIteration() {
		return iteration;
	}
	public boolean kInductionEnabled() {
		return k_induction;
	}
	public boolean invariantGenerationEnabled() {
		return invariant_gen;
	}
	public int getPdrThreads() {
		return pdr_threads;
	}

}
