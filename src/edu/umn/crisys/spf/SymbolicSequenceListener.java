//
//Copyright (C) 2007 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
package edu.umn.crisys.spf;

// does not work well for static methods:summary not printed for errors
import gov.nasa.jpf.symbc.sequences.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.concolic.PCAnalyzer;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicConstraintsGeneral;
import gov.nasa.jpf.symbc.string.DerivedStringExpression;
import gov.nasa.jpf.symbc.string.StringSymbolic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author Mithun Acharya
 * with inputs from Corina.
 *
 * Note that all the methods of interest should be specified in +symbolic.method option.
 * if a method is not specified in +symbolic.method it will not be printed.
 * even if the method, foo(int i) is invoked concretely always, we should have
 * foo(con) in +symbolic.method option
 *
 * Algorithm (Works independent of search order):
 *
 * When instructionExecuted->InvokeInstruction, remember the executed method, symbolic attributes, etc.
 * in a SequenceChoiceGenerator
 *
 * The main idea is to exploit the fact that
 * "at each state, the path from start state to the current state has a
 * unique chain of choice generators"
 *
 * During stateBacktracked or propertyViolated, get the chain of choice generators. In this chain look
 * for SequenceChoiceGenerators (which hold information about symbolically executed methods).
 * With the current path condition solution and the symbolic attributes
 * stored in SequenceChoiceGenerators, output the concrete method sequence.
 *
 *
 * KNOWN PROBLEMS:
 *
 * 1) For JUnit test cases, getting class name and object name is not smart.
 *
 *
 */
public class SymbolicSequenceListener extends PropertyListenerAdapter implements PublisherExtension {


	// this set will store all the method sequences.
	// will be printed at last.
	// 'methodSequences' is a set of 'methodSequence's
	// A single 'methodSequence' is a vector of invoked 'method's along a path
	// A single invoked 'method' is represented as a String.
 	List<List<String>> methodSequences = new ArrayList<List<String>>();

 	// Name of the class under test
 	String className ="";

 	// custom marker to mark error strings in method sequences
 	private final static String exceptionMarker = "##EXCEPTION## ";
 	private File sequenceOutputFile;

	public SymbolicSequenceListener(Config conf, JPF jpf) {
		jpf.addPublisherExtension(ConsolePublisher.class, this);
		sequenceOutputFile = conf.getPath("seqlistener.output");
	}

	@Override
	public void propertyViolated (Search search){
		System.out.println("--------->property violated");
		VM vm = search.getVM();
		SystemState ss = vm.getSystemState();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		if (!(cg instanceof PCChoiceGenerator)){
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
				prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}
		Property prop = search.getLastError().getProperty();
		String errAnn="";
		if (prop instanceof NoUncaughtExceptionsProperty) {
			String exceptionClass=((NoUncaughtExceptionsProperty)prop).getUncaughtExceptionInfo().getExceptionClassname();
			errAnn = "(expected = "+ exceptionClass +".class)";
		}

		String error = search.getLastError().getDetails();
		error = "\"" + error.substring(0,error.indexOf("\n")) + "...\"";

		if ((cg instanceof PCChoiceGenerator) &&
				      ((PCChoiceGenerator) cg).getCurrentPC() != null){

			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
			System.out.println("pc "+ pc.count() + " "+pc);

			//solve the path condition
			if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
				SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
				PCAnalyzer pa = new PCAnalyzer();
				pa.solve(pc,solver);
			}
			else
				pc.solve();

			// get the chain of choice generators.
			ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
			List<String> methodSequence = getMethodSequence(cgs);
			// Now append the error String and then add methodSequence to methodSequences
			// prefix the exception marker to distinguish this from
			// an invoked method.
			if (errAnn!="")
				methodSequence.add(0,errAnn);
			methodSequence.add(exceptionMarker + error);
			methodSequences.add(methodSequence);
		}
	}




	@Override
	 public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {



		if (!vm.getSystemState().isIgnored()) {
			Instruction insn = executedInstruction;
			SystemState ss = vm.getSystemState();
			ThreadInfo ti = currentThread;
			Config conf  = vm.getConfig();

			if (insn instanceof InvokeInstruction && insn.isCompleted(ti)) {
				InvokeInstruction md = (InvokeInstruction) insn;
				String methodName = md.getInvokedMethodName();
				int numberOfArgs = md.getArgumentValues(ti).length;
				MethodInfo mi = md.getInvokedMethod();

				StackFrame sf = ti.getTopFrame();
				String shortName = methodName;
				//String longName = mi.getLongName();
				if (methodName.contains("("))
					shortName = methodName.substring(0,methodName.indexOf("("));
				// does not work for recursive invocations of sym methods; should compare MethodInfo instead
				//if(!shortName.equals(sf.getMethodName()))
					//return;
				if(!mi.equals(sf.getMethodInfo()))
					return;

				if ((BytecodeUtils.isMethodSymbolic(conf, mi.getFullName(), numberOfArgs, null))){
					// FIXME: get the object name?
					// VirtualInvocation virtualInvocation = (VirtualInvocation)insn;
					// int ref = virtualInvocation.getThis(ti);
					// DynamicElementInfo d = ss.ks.da.get(ref);
					// right now I am just getting the class name
					className = mi.getClassName();


					// get arg values
					Object [] argValues = md.getArgumentValues(ti);

					// get symbolic attributes
					// concretely executed method will have null attributes.
					// TODO: fix there

					byte[] argTypes = mi.getArgumentTypes();
					Object[] attributes = new Object[numberOfArgs];

					int count = 1 ; // we do not care about this
					if (md instanceof INVOKESTATIC)
						count = 0;  //no "this" reference
					for (int i = 0; i < numberOfArgs; i++) {
						attributes[i] = sf.getLocalAttr(count);
						count++;
						if(argTypes[i]== Types.T_LONG || argTypes[i] == Types.T_DOUBLE)
							count++;
					}

					// Create a new SequenceChoiceGenerator.
					// this is just to store the information
					// regarding the executed method.
					SequenceChoiceGenerator cg = new SequenceChoiceGenerator(shortName);
					// Let's grab concrete strings now and not later. If the 
					// system under test doesn't save this concrete string 
					// somewhere, it'll be garbage collected (or something)
					// and when we try to do getMethodSequence(), it won't be
					// there for us to read. Right now, though, of course, it's
					// available, since this instruction is just now executing.
					for (int i=0; i<argValues.length; i++) {
						if (attributes[i] == null &&
								argValues[i] instanceof DynamicElementInfo
					            && ((DynamicElementInfo) argValues[i]).isStringObject()) {
							argValues[i] = getStringOfArg(argValues[i], null);
						}
					}
					

					
					
					cg.setArgValues(argValues);
					cg.setArgAttributes(attributes);
					
					// Does not actually make any choice
					ss.setNextChoiceGenerator(cg);
					// nothing to do as there are no choices.
				}
			}
			//else if (insn instanceof ReturnInstruction){
			// I don't think we need to do anything  here for printing method sequences...
			//}
		}

	}



	@Override
    public void threadTerminated(VM vm, ThreadInfo terminatedThread) {
        // TODO Auto-generated method stub

        super.threadTerminated(vm, terminatedThread);
    }

	
	public void stateBacktracked(Search search) {
	    storeSequence(search);
	}

    public void storeSequence(Search search) {
		VM vm = search.getVM();
		Config conf = vm.getConfig();

		Instruction insn = vm.getChoiceGenerator().getInsn();
		SystemState ss = vm.getSystemState();
		//ThreadInfo ti = vm.getChoiceGenerator().getThreadInfo();
		MethodInfo mi = insn.getMethodInfo();
		String methodName = mi.getFullName();

		int numberOfArgs = mi.getNumberOfArguments();//mi.getArgumentsSize()- 1;// corina: problem here? - 1;

	//	if (BytecodeUtils.isMethodSymbolic(conf, methodName, numberOfArgs, null)){

			ChoiceGenerator<?> cg = vm.getChoiceGenerator();

			if (!(cg instanceof PCChoiceGenerator)){
				ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
				while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
						prev_cg = prev_cg.getPreviousChoiceGenerator();
				}
				cg = prev_cg;
			}

			if ((cg instanceof PCChoiceGenerator) &&
				      ((PCChoiceGenerator) cg).getCurrentPC() != null){

				PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
				//solve the path condition
				if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
					SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
					PCAnalyzer pa = new PCAnalyzer();
					pa.solve(pc,solver);
				}
				else
					pc.solve();
				// get the chain of choice generators.
				ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
				List<String> methodSeq = getMethodSequence(cgs);
				removePrefixesOf(methodSeq);
				if ( ! isPrefix(methodSeq)) {
				    methodSequences.add(methodSeq);
				}
			}
	//	}
	}
    
    private boolean firstIsPrefix(List<String> first, List<String> second) {
        if (first.size() > second.size()) {
            return false;
        }
        for (int i=0; i < first.size(); i++) {
            if ( ! first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isPrefix(List<String> methodSeq) {
        for (List<String> existing : methodSequences) {
            if (firstIsPrefix(methodSeq, existing)) {
                return true;
            }
        }
        return false;
    }

    private void removePrefixesOf(List<String> methodSeq) {
        Iterator<List<String>> iter = methodSequences.iterator();
        while (iter.hasNext()) {
            List<String> otherSeq = iter.next();
            
            if (firstIsPrefix(otherSeq, methodSeq)) {
                iter.remove();
            }
            
        }
    }
    
	/**
	 *
	 * traverses the ChoiceGenerator chain to get the method sequence
	 * looks for SequenceChoiceGenerator in the chain
	 * SequenceChoiceGenerators have information about the methods
	 * executed and hence the method sequence can be obtained.
	 * A single 'methodSequence' is a vector of invoked 'method's along a path
	 * A single invoked 'method' is represented as a String.
	 *
	 */
	private List<String> getMethodSequence(ChoiceGenerator [] cgs){
		// A method sequence is a vector of strings
		List<String> methodSequence = new ArrayList<String>();
		ChoiceGenerator cg = null;
		// explore the choice generator chain - unique for a given path.
		for (int i=0; i<cgs.length; i++){
			cg = cgs[i];
			if ((cg instanceof SequenceChoiceGenerator)){
				methodSequence.add(getInvokedMethod((SequenceChoiceGenerator)cg));
			}
		}
		return methodSequence;
	}



	/**
	 * A single invoked 'method' is represented as a String.
	 * information about the invoked method is got from the SequenceChoiceGenerator
	 */
	private String getInvokedMethod(SequenceChoiceGenerator cg){
		String invokedMethod = "";

		// get method name
		String shortName = cg.getMethodShortName();
		invokedMethod +=  shortName + "(";

		// get argument values
		Object[] argValues = cg.getArgValues();

		// get symbolic attributes
		Object[] attributes = cg.getArgAttributes();

		// get the solution
		for(int i=0; i<argValues.length; i++){
			invokedMethod += getStringOfArg(argValues[i], attributes[i]) + ",";
		}

		// remove the extra comma
		if (invokedMethod.endsWith(","))
			invokedMethod = invokedMethod.substring(0,invokedMethod.length()-1);
		invokedMethod += ")";

		return invokedMethod;
	}

	private String getStringOfArg(Object argValue, Object attribute) {
		if (attribute != null){ // parameter symbolic
			// here we should consider different types of symbolic arguments
			//IntegerExpression e = (IntegerExpression)attributes[i];
			String solution = "";


			if(attribute instanceof IntegerExpression) {
				// trick to print bools correctly
				if(argValue.toString()=="true" || argValue.toString()=="false") {
					if(((IntegerExpression) attribute).solution() == 0)
						solution = solution+ "false";
					else
						solution = solution+ "true";
				}
				else
					solution = solution+ ((IntegerExpression) attribute).solution();
			}
			else if (attribute instanceof RealExpression)
				solution = solution+ ((RealExpression) attribute).solution();
			else if (attribute instanceof DerivedStringExpression) {
				DerivedStringExpression dse = (DerivedStringExpression) attribute;
				if (dse.right == null && dse.left == null) {
					// This is gonna NullPointer at us.
					solution += "derived_string_both_null";
					System.err.println("Derived string expression had both sides null. Operator was "+dse.op);
				} else if (dse.left == null) {
					// Ah, right isn't null though! Let's use that.
					// For some reason, the default one doesn't check this.
					solution += dse.right.solution();
				}
				else {
					solution = solution+ dse.solution();
				}
			}
			else
				solution = solution+ ((StringSymbolic) attribute).solution();
			return solution;
		}
		else { // parameter concrete - for a concrete parameter, the symbolic attribute is null
		    if (argValue instanceof DynamicElementInfo
		            && ((DynamicElementInfo) argValue).isStringObject()) {
		    	try {
		    		DynamicElementInfo element = (DynamicElementInfo) argValue;
			        return "\""+element.asString()+"\"";
		    	} catch (NullPointerException e) {
		    		return "object_removed_from_heap_before_i_could_grab_its_value";
		    	}
		    } else {
		        return argValue+"";
		    }
		}

	}


      //	-------- the publisher interface
	public void publishFinished (Publisher publisher) {

		publisher.publishTopicStart("Method Sequences");
		PrintWriter pw = publisher.getOut();

		
		if (sequenceOutputFile != null) {
			try {
				printMethodSequences(new PrintWriter(sequenceOutputFile));
				pw.println("Sequences written to "+sequenceOutputFile);
			} catch (FileNotFoundException e) {
				System.err.println("File not found: "+sequenceOutputFile);
			}
		} else {
		
			// here just print the method sequences
			printMethodSequences(pw);
		}

		// print JUnit4.0 test class
		//publisher.publishTopicStart("JUnit 4.0 test class");
		//printJUnitTestClass(pw);

	}


	  /**
	   * @author Mithun Acharya
	   *
	   * prints the method sequences
	   */
	  private void printMethodSequences(PrintWriter pw){
		  Iterator<List<String>> it = methodSequences.iterator();
		  pw.println("Number of tests: "+methodSequences.size());
		  while (it.hasNext()){
			  pw.println(it.next());
		  }
		  pw.flush();
	  }


	  /**
	   * @author Mithun Acharya
	   * Dumb printing of JUnit 4.0 test class
	   * FIXME: getting class name and object name is not smart.
	   */
	  private void printJUnitTestClass(PrintWriter pw){
		  // imports
		  pw.println("import static org.junit.Assert.*;");
		  pw.println("import org.junit.Before;");
		  pw.println("import org.junit.Test;");

		  String objectName = (className.toLowerCase()).replace(".", "_");

		  pw.println();
		  pw.println("public class " + className.replace(".", "_") + "Test {"); // test class
		  pw.println();
		  pw.println("	private " + className + " " + objectName + ";"); // CUT object to be tested
		  pw.println();
		  pw.println("	@Before"); // setUp method annotation
		  pw.println("	public void setUp() throws Exception {"); // setUp method
		  pw.println("		" + objectName + " = new " + className + "();"); // create object for CUT
		  pw.println("	}"); // setUp method end
		  // Create a test method for each sequence
		  int testIndex = 0;
		  Iterator<List<String>> it = methodSequences.iterator();
		  while (it.hasNext()){
			  List<String> methodSequence = it.next();
			  pw.println();
			  Iterator<String> it1 = methodSequence.iterator();
			  if (it1.hasNext()) {
				  String errAnn = (String)(it1.next());

				  if (errAnn.contains("expected")) {
					  pw.println("	@Test"+errAnn); // Corina: added @Test annotation with exception expected
				  }
				  else {
					  pw.println("	@Test"); // @Test annotation
					  it1 = methodSequence.iterator();
				  }
			  }
			  else
				  it1 = methodSequence.iterator();

			  //pw.println("	@Test"); // @Test annotation
			  pw.println("	public void test" + testIndex + "() {"); // begin test method
			  //Iterator<String> it1 = methodSequence.iterator();
			  while(it1.hasNext()){
				  String invokedMethod = it1.next();
				  if (invokedMethod.contains(exceptionMarker)) { // error-string. not a method
					  // add a comment about the exception
					  pw.println("		" + "//should lead to " + invokedMethod);
				  }
				  else{ // normal method
					  pw.println("		" + objectName + "." + invokedMethod + ";"); // invoke a method in the sequence
				  }
			  }
			  pw.println("	}"); // end test method
			  testIndex++;
		  }
		  pw.println("}"); // test class end
	  }

}
