package edu.umn.crisys.plexil.ast.globaldecl;

import edu.umn.crisys.plexil.ast.expr.ASTExpression;
import edu.umn.crisys.plexil.java.values.PlexilType;

public class VariableDecl {

	private String name;
	private int arraySize = -1;
	private PlexilType type;
	private ASTExpression init;
	
	public VariableDecl(String name, PlexilType type) {
		this(name, type, null);
	}
	
	public VariableDecl(String name, PlexilType type, ASTExpression init) {
		if (type.isArrayType()) {
			throw new RuntimeException("Tried to create a "+type+" with no size");
		}
		this.name = name;
		this.type = type;
		this.init = init;
	}
	
	public VariableDecl(String arrayName, int arraySize, PlexilType type) {
		this(arrayName, arraySize, type, null);
	}
	
	public VariableDecl(String arrayName, int arraySize, PlexilType type, ASTExpression init) {
		if ( ! type.isArrayType()) {
			throw new RuntimeException("Passed in a size, but "+type+" isn't an array type");
		}
		if (arraySize < 0) { 
            throw new RuntimeException("Array cannot have negative size: "+arraySize);
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
		return arraySize >= 0;
	}
	
	public int getArraySize() {
		if (! isArray()) {
			throw new RuntimeException(name+" is not an array!");
		}
		return arraySize;
	}
	
	public boolean hasInitialValue() {
		return init != null;
	}
	
	public ASTExpression getInitialValue() {
		if (init == null) {
			throw new NullPointerException("No initial value for "+name);
		}
		return init;
	}
	
	public String toString() {
		PlexilType typeToUse = type;
		String arrayStuff = "";
		String init = "";
		
        if (this.isArray()) {
            typeToUse = typeToUse.elementType();
            arrayStuff = "["+arraySize+"]";
        }
        
        if (this.hasInitialValue()) {
        	init = " = "+init;
        }
        
		// Start with the type, uncapitalizing all but the first letter
        String typeStr = typeToUse.toString().charAt(0) 
            + typeToUse.toString().substring(1).toLowerCase();
        
        return typeStr+" "+name+arrayStuff+init+";";
	}
	
}
