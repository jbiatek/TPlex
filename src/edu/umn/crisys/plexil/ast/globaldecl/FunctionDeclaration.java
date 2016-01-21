package edu.umn.crisys.plexil.ast.globaldecl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	private Optional<VariableDecl> ret = Optional.empty();
	
	public FunctionDeclaration(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Optional<VariableDecl> getReturnValue() {
		return ret;
	}
	
	public void setReturnValue(VariableDecl ret) {
		this.ret = Optional.of(ret);
	}
	
	public List<VariableDecl> getParameters() {
		return parameters;
	}
	
	public void addParameter(VariableDecl param) {
		parameters.add(param);
	}

	@Override
	public String toString() {
		return ret.map(v -> v.getType().toString()+" ").orElse("")
				+ name 
				+ parameters.stream()
					.map(v -> v.getType().toString())
					.collect(Collectors.joining(", ", "(", ")"));
	}
}
