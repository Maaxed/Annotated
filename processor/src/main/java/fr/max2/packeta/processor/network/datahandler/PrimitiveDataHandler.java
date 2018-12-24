package fr.max2.packeta.processor.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.processor.network.DataHandlerParameters;

public enum PrimitiveDataHandler implements INamedDataHandler
{
	// Integers
	BYTE,
	SHORT,
	INT,
	LONG,
	
	//Floats
	FLOAT,
	DOUBLE,
	
	// Other primitives
	BOOLEAN,
	CHAR;
	
	private final String primitiveName;
	public final TypeKind kind;
	
	private PrimitiveDataHandler()
	{
		String enumName = this.name();
		this.primitiveName = enumName.charAt(0) + enumName.substring(1).toLowerCase(); //To camel case
		this.kind = TypeKind.valueOf(enumName);
	}

	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		DataHandlerUtils.addBufferInstructions(this.primitiveName, params.saveAccessExpr, params.setExpr, saveInstructions, loadInstructions);
	}
	
	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		TypeMirror thisType = this.getType(elemUtils, typeUtils);
		return type -> typeUtils.isAssignable(type, thisType) && typeUtils.isAssignable(thisType, type);
	}
	
	@Override
	public TypeMirror getType(Elements elemUtils, Types typeUtils)
	{
		return typeUtils.getPrimitiveType(this.kind);
	}

	@Override
	public String getTypeName()
	{
		return this.primitiveName.toLowerCase();
	}
	
}
