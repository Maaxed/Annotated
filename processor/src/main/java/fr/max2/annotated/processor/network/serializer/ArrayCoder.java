package fr.max2.annotated.processor.network.serializer;

import java.util.List;

import javax.lang.model.type.ArrayType;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class ArrayCoder
{
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return new TypedDataHandler<>(tools, tools.types.getArrayType(tools.elements.objectElement.asType()), true, fieldType ->
		{
			ArrayType arrayType = tools.types.asArrayType(fieldType);
			if (arrayType == null)
				throw new IncompatibleTypeException("The type '" + fieldType + "' is not an array");
			
			SerializationCoder contentCoder = tools.coders.getCoder(arrayType.getComponentType());
			String constructor = tools.naming.erasedType.get(fieldType) + "::new";

			return new GenericCoder(tools, fieldType, "fr.max2.annotated.lib.network.serializer.ObjectArraySerializer",
				params -> params.add(constructor), List.of(contentCoder));
		});
	}
}
