package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IParameterConsumer;
import fr.max2.annotated.processor.network.model.IParameterSupplier;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class ParametrizedCoder extends GenericCoder
{
	public ParametrizedCoder(ProcessingTools tools, TypeMirror type, String serializer, IParameterSupplier parameterCoder, List<SerializationCoder> parameterCoders)
	{
		super(tools, type, serializer, params ->
		{
			parameterCoder.pipe(params);
			parameterCoders.stream().map(SerializationCoder::codeSerializerInstance).forEach(params::add);
		});
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, BiConsumer<TypeMirror, IParameterConsumer> parameterCoder, ISubCoderProvider coderProvider)
	{
		return new NamedDataHandler<>(tools, typeName, (t, paramType) -> new ParametrizedCoder(tools, paramType, serializer, params -> parameterCoder.accept(paramType, params), coderProvider.buildSubCoders(paramType)));
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, BiConsumer<TypeMirror, IParameterConsumer> parameterCoder, int expectedTypeArgCount, IImplementationProvider implProvider)
	{
		DeclaredType collectionType = tools.types.asDeclared(tools.elements.getTypeElement(typeName).asType());
		int argCount = collectionType.getTypeArguments().size();
		if (argCount != expectedTypeArgCount)
			throw new IllegalArgumentException("The type '" + typeName + "' has the wrong number of variable types. Expected " + expectedTypeArgCount + " but has " + argCount);
		
		return handler(tools, typeName, serializer, parameterCoder, fieldType ->
		{
			DeclaredType refinedType = tools.types.refineTo(fieldType, collectionType);
			if (refinedType == null)
				throw new IncompatibleTypeException("The type '" + fieldType + "' is not a sub type of " + typeName);
			
			List<SerializationCoder> argCoders = new ArrayList<>();
			for (TypeMirror arg : refinedType.getTypeArguments())
			{
				if (arg.getKind() == TypeKind.WILDCARD)
					throw new IncompatibleTypeException("Wildcards are not supported in type arguments: '" + refinedType + "'");
				
				argCoders.add(tools.coders.getCoder(arg));
			}
			
			TypeMirror rawImplType = implProvider.findImplementation(fieldType);

			DeclaredType refinedImplType = tools.types.refineTo(rawImplType, collectionType);
			if (refinedImplType == null)
				throw new IncompatibleTypeException("The implementation type '" + rawImplType + "' is not a sub type of " + typeName);
			
			DeclaredType implType = tools.types.asDeclared(rawImplType);
			for (int argIndex = 0; argIndex < expectedTypeArgCount; argIndex++)
			{
				TypeMirror arg = refinedImplType.getTypeArguments().get(argIndex);
				if (arg.getKind() == TypeKind.WILDCARD)
					throw new IncompatibleTypeException("Wildcards are not supported in type arguments: '" + refinedType + "'");
				
				implType = tools.types.replaceTypeArgument(implType, arg, refinedType.getTypeArguments().get(argIndex));
			}
			
			if (!tools.types.isAssignable(implType, fieldType))
				throw new IncompatibleTypeException("The implementation type '" + implType + "' is not a sub-type of '" + fieldType + "'");
			
			return argCoders;
		});
	}
	
	public static interface ISubCoderProvider
	{
		List<SerializationCoder> buildSubCoders(TypeMirror type) throws CoderException;
	}
	
	public static interface IImplementationProvider
	{
		TypeMirror findImplementation(TypeMirror type) throws CoderException;
	}
}
