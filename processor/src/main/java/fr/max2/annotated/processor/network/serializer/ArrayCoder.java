package fr.max2.annotated.processor.network.serializer;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class ArrayCoder
{
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return new SpecialDataHandler<>(TypeKind.ARRAY, fieldType ->
		{
			ArrayType arrayType = tools.types.asArrayType(fieldType);
			if (arrayType == null) throw new IncompatibleTypeException("The type '" + fieldType + "' is not an array");
			
			TypeMirror contentType = arrayType.getComponentType();
			
			if (contentType.getKind().isPrimitive())
			{
				return new SimpleCoder(tools, fieldType, "fr.max2.annotated.lib.network.serializer.PrimitiveArraySerializer." + capitalize(tools.naming.erasedType.get(contentType)) + "ArraySerializer");
			}
			SerializationCoder contentCoder = tools.coders.getCoder(contentType);

			return new GenericCoder(tools, fieldType, "fr.max2.annotated.lib.network.serializer.ObjectArraySerializer",
				params -> params.add(tools.naming.erasedType.get(fieldType) + "::new"), List.of(contentCoder));
		});
	}
	
	private static String capitalize(String str)
	{
		if (str.isEmpty())
			return "";
		
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
