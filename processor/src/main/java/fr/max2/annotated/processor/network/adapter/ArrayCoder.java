package fr.max2.annotated.processor.network.adapter;

import java.util.List;

import javax.lang.model.type.ArrayType;

import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class ArrayCoder
{
	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools)
	{
		return new TypedDataHandler<>(tools, tools.types.getArrayType(tools.elements.objectElement.asType()), true, fieldType ->
		{
			ArrayType arrayType = tools.types.asArrayType(fieldType);
			if (arrayType == null)
				throw new IncompatibleTypeException("The type '" + fieldType + "' is not an array");

			AdapterCoder contentCoder = tools.adapterCoders.getCoder(arrayType.getComponentType());

			ArrayType toType = tools.types.getArrayType(contentCoder.typeTo);

			String fromConstructor = tools.naming.erasedType.get(fieldType) + "::new";
			String toConstructor = tools.naming.erasedType.get(toType) + "::new";

			return GenericCoder.build(tools, fieldType, toType, "fr.max2.annotated.lib.network.adapter.ObjectArrayAdapter",
				params -> params.addAll(fromConstructor, toConstructor), List.of(contentCoder));
		});
	}
}
