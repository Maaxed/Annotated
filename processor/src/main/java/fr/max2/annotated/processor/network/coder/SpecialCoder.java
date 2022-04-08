package fr.max2.annotated.processor.network.coder;

import javax.annotation.Nullable;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import fr.max2.annotated.api.processor.network.DataProperties;
import fr.max2.annotated.processor.network.coder.handler.IDataCoderProvider;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SpecialCoder extends DataCoder
{
	public static final IDataHandler WILDCRD = handler(TypeKind.WILDCARD, (tools, uniqueName, paramType, properties) ->
	{
		WildcardType wildcardType = tools.types.asWildcardType(paramType);
		if (wildcardType == null) throw incompatibleType("wildcard", paramType);
		
		TypeMirror extendsBound = wildcardType.getExtendsBound();
		
		if (extendsBound == null) throw new IncompatibleTypeException("The wildcard type '" + paramType + "' has no extends bound so it cannot be handled");
		
		return tools.coders.getCoder(uniqueName, extendsBound, properties);
	}),
	VARIABLE_TYPE = handler(TypeKind.TYPEVAR, (tools, uniqueName, paramType, properties) ->
	{
		TypeVariable varType = tools.types.asVariableType(paramType);
		if (varType == null) throw incompatibleType("variable", paramType);
		
		return tools.coders.getCoder(uniqueName, varType.getUpperBound(), properties);
	}),
	INTERSECTION = handler(TypeKind.INTERSECTION, (tools, uniqueName, paramType, properties) ->
	{
		IntersectionType intersectionType = tools.types.asIntersectionType(paramType);
		if (intersectionType == null) throw incompatibleType("intersection", paramType);
		
		DataCoder validCoder = null;
		
		for (TypeMirror type : intersectionType.getBounds())
		{
			DataCoder newCoder;
			try
			{
				newCoder = tools.coders.getCoderOrNull(uniqueName, type, properties);
			}
			catch (CoderExcepetion e)
			{
				newCoder = null; // Skip the exception
			}
			
			if (newCoder != null && validCoder != null)
				throw new IncompatibleTypeException("Too many bounds of the interaction type '" + paramType + "' have a valid data coder");
			
			validCoder = newCoder;
		}
		
		if (validCoder == null)
			throw new IncompatibleTypeException("No data coder found for any of the bounds of the interaction type '" + paramType + "'");
		
		return validCoder;
	}),
	DEFAULT = handler(null, (tools, uniqueName, paramType, properties) ->
	{
		IDataHandler handler = tools.coders.getDefaultHandler(paramType);
		if (handler == null)
			throw new IncompatibleTypeException("No data coder found to process the '" + paramType + "' type. Use the " + DataProperties.class.getCanonicalName() + " annotation with the 'type' property to specify a DataType");
		
		return handler.createCoder(tools, uniqueName, paramType, properties);
	});
	
	protected DataCoder actualCoder;
	
	private SpecialCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, DataCoder actualCoder)
	{
		super(tools, uniqueName, paramType, properties, tools.types.isSameType(actualCoder.getInternalType(), actualCoder.paramType) ? paramType : actualCoder.getInternalType());
		this.actualCoder = actualCoder;
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		return builder.runCoder(this.actualCoder, saveAccessExpr, internalAccessExpr, externalAccessExpr);
	}
	
	private static RuntimeException incompatibleType(String expected, TypeMirror actual) throws IncompatibleTypeException
	{
		throw new IncompatibleTypeException("The type '" + actual + "' is not a valid " + expected + " type");
	}
	
	private static IDataHandler handler(@Nullable TypeKind kind, IDataCoderProvider coderProvider)
	{
		return new SpecialDataHandler(kind, (tools, uniqueName, paramType, properties) -> new SpecialCoder(tools, uniqueName, paramType, properties, coderProvider.createCoder(tools, uniqueName, paramType, properties)));
	}
}
