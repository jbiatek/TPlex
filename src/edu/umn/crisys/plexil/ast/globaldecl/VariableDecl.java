package edu.umn.crisys.plexil.ast.globaldecl;

import java.util.Optional;

import edu.umn.crisys.plexil.expr.il.ILExpr;
import edu.umn.crisys.plexil.expr.il.ILType;

public class VariableDecl {

	private String name;
	private ILType type;
	private Optional<Integer> arraySize;
	private Optional<? extends ILExpr> init;
	
	public VariableDecl(String name, ILType type) {
		this(name, type, Optional.empty(), Optional.empty());
	}
	
	public VariableDecl(String name, ILType type, ILExpr init) {
		this(name, type, Optional.empty(), Optional.of(init));
	}
	
	public VariableDecl(String arrayName, int arraySize, ILType type) {
		this(arrayName, type, Optional.of(arraySize), Optional.empty());
	}
	
	public VariableDecl(String arrayName, int arraySize, ILType type, ILExpr init) {
		this(arrayName, type, Optional.of(arraySize), Optional.of(init));
	}
	
	public VariableDecl(String arrayName, final ILType type, 
			Optional<Integer> arraySize, Optional<? extends ILExpr> init) {
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
	
	public ILType getType() {
		return type;
	}
	
	public boolean isArray() {
		return arraySize.isPresent();
	}
	
	public Optional<Integer> getArraySize() {
		return arraySize;
	}
	
	public Optional<? extends ILExpr> getInitialValue() {
		return init;
	}
	
	public String toString() {
		ILType typeToUse = type;
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
