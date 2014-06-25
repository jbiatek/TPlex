package edu.umn.crisys.plexil.ast.globaldecl;

import java.util.ArrayList;
import java.util.List;

/**
 * PLEXIL doesn't actually have functions, but it does have two things 
 * that share a lot in common: Commands and Lookups. This class lets us 
 * combine the boilerplate for both of them.
 * 
 * @author jbiatek
 *
 */
public abstract class FunctionDeclaration {

	private String name;
	private List<VariableDecl> parameters = new ArrayList<VariableDecl>();
	private VariableDecl ret;
	
	public FunctionDeclaration(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasReturnValue() {
		return ret != null;
	}
	
	public VariableDecl getReturnValue() {
		if (! hasReturnValue()) {
			throw new NullPointerException(name+" has no return value");
		}
		return ret;
	}
	
	public void setReturnValue(VariableDecl ret) {
		this.ret = ret;
	}
	
	public List<VariableDecl> getParameters() {
		return parameters;
	}
	
	public void addParameter(VariableDecl param) {
		parameters.add(param);
	}

}
