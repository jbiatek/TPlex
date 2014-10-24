package edu.umn.crisys.plexil.ast.globaldecl;

import java.util.Optional;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.runtime.values.PlexilType;

public class VariableDecl {

	private String name;
	private PlexilType type;
	private Optional<Integer> arraySize;
	private Optional<? extends ASTExpression> init;
	
	public VariableDecl(String name, PlexilType type) {
		this(name, type, Optional.empty(), Optional.empty());
	}
	
	public VariableDecl(String name, PlexilType type, ASTExpression init) {
		this(name, type, Optional.empty(), Optional.of(init));
	}
	
	public VariableDecl(String arrayName, int arraySize, PlexilType type) {
		this(arrayName, type, Optional.of(arraySize), Optional.empty());
	}
	
	public VariableDecl(String arrayName, int arraySize, PlexilType type, ASTExpression init) {
		this(arrayName, type, Optional.of(arraySize), Optional.of(init));
	}
	
	public VariableDecl(String arrayName, final PlexilType type, 
			Optional<Integer> arraySize, Optional<? extends ASTExpression> init) {
		arraySize.ifPresent((size) -> {
			if (size < 0) { 
				throw new RuntimeException("Array cannot have negative size: "+arraySize);
			} else if (! type.isArrayType()) {
				throw new RuntimeException("Passed in a size, but "+type+" isn't an array type");
			}
		});
		if (type.isArrayType() && ! arraySize.isPresent()) {
			throw new RuntimeException("Tried to create a "+type+" with no size");
		}
		
		this.name = arrayName;
		this.arraySize = arraySize;
		this.type = type;
		this.init = init;
	}
	
	public String getName() {
		return name;
	}
	
	public PlexilType getType() {
		return type;
	}
	
	public boolean isArray() {
		return arraySize.isPresent();
	}
	
	public Optional<Integer> getArraySize() {
		return arraySize;
	}
	
	public Optional<? extends ASTExpression> getInitialValue() {
		return init;
	}
	
	public String toString() {
		PlexilType typeToUse = type;
		String arrayStuff = "";
		StringBuilder initString = new StringBuilder();
		
        if (this.isArray()) {
            typeToUse = typeToUse.elementType();
            arrayStuff = "["+arraySize.get()+"]";
        }
        
        getInitialValue().ifPresent((initValue) -> initString.append(" = "+initValue));
        
		// Start with the type, uncapitalizing all but the first letter
        String typeStr = typeToUse.toString().charAt(0) 
            + typeToUse.toString().substring(1).toLowerCase();
        
        return typeStr+" "+name+arrayStuff+initString+";";
	}
	
}
