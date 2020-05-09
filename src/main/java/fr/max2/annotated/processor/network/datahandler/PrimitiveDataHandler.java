package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

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
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DataHandlerUtils.addBufferInstructions(this.primitiveName, params.saveAccessExpr, params.setExpr, builder);
	}
	
	@Override
	public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
	{
		TypeMirror thisType = this.getType(elemUtils, typeUtils);
		return type -> typeUtils.isAssignable(type, thisType) && typeUtils.isAssignable(thisType, type);
	}
	
	@Override
	public TypeMirror getType(ExtendedElements elemUtils, ExtendedTypes typeUtils)
	{
		return typeUtils.getPrimitiveType(this.kind);
	}

	@Override
	public String getTypeName()
	{
		return this.primitiveName.toLowerCase();
	}
	
}
