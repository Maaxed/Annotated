package fr.max2.annotated.processor.network.serializer;

import javax.annotation.Nullable;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.ICoderProvider;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public final class SpecialCoder
{
	private SpecialCoder()
	{ }
	
	public static ICoderHandler<SerializationCoder> wildcard(ProcessingTools tools)
	{
		return handler(TypeKind.WILDCARD, paramType ->
		{
			WildcardType wildcardType = tools.types.asWildcardType(paramType);
			if (wildcardType == null) throw incompatibleType("wildcard", paramType);
			
			TypeMirror extendsBound = wildcardType.getExtendsBound();
			
			if (extendsBound == null) throw new IncompatibleTypeException("The wildcard type '" + paramType + "' has no extends bound so it cannot be handled");
			
			return tools.coders.getCoder(extendsBound);
		});
	}
	
	public static ICoderHandler<SerializationCoder> variableType(ProcessingTools tools)
	{
		return handler(TypeKind.TYPEVAR, paramType ->
		{
			TypeVariable varType = tools.types.asVariableType(paramType);
			if (varType == null) throw incompatibleType("variable", paramType);
			
			return tools.coders.getCoder(varType.getUpperBound());
		});
	}
	
	public static ICoderHandler<SerializationCoder> intersection(ProcessingTools tools)
	{
		return handler(TypeKind.INTERSECTION, paramType ->
		{
			IntersectionType intersectionType = tools.types.asIntersectionType(paramType);
			if (intersectionType == null) throw incompatibleType("intersection", paramType);
			
			SerializationCoder validCoder = null;
			
			for (TypeMirror type : intersectionType.getBounds())
			{
				SerializationCoder newCoder;
				try
				{
					newCoder = tools.coders.getCoderOrNull(type);
				}
				catch (CoderException e)
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
		});
	}
	
	private static RuntimeException incompatibleType(String expected, TypeMirror actual) throws IncompatibleTypeException
	{
		throw new IncompatibleTypeException("The type '" + actual + "' is not a valid " + expected + " type");
	}
	
	private static ICoderHandler<SerializationCoder> handler(@Nullable TypeKind kind, ICoderProvider<SerializationCoder> coderProvider)
	{
		return new SpecialDataHandler<>(kind, coderProvider);
	}
}
