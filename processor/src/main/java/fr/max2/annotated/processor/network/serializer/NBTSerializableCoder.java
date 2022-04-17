package fr.max2.annotated.processor.network.serializer;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class NBTSerializableCoder
{
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return ParametrizedCoder.handler(tools, ClassRef.NBT_SERIALIZABLE_INTERFACE, "fr.max2.annotated.lib.network.serializer.NBTSerializableSerializer",
			(fieldType, params) -> params.add(tools.naming.erasedType.get(fieldType) + "::new"),
			fieldType ->
			{
				String typeName = ClassRef.NBT_SERIALIZABLE_INTERFACE;
				TypeMirror serializableType = tools.elements.getTypeElement(typeName).asType();
				
				DeclaredType serialisableType = tools.types.refineTo(fieldType, serializableType);
				if (serialisableType == null)
					throw new IncompatibleTypeException("The type '" + fieldType + "' is not a sub type of " + typeName);
				
				TypeMirror nbtType = serialisableType.getTypeArguments().get(0);
				SerializationCoder nbtCoder = tools.coders.getCoder(nbtType);
				
				TypeMirror implType = fieldType;
				SerializationCoder.requireDefaultConstructor(tools.types, implType, null);
				
				return List.of(nbtCoder);
			});
	}
}
