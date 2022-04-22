package fr.max2.annotated.processor.network.serializer;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;

public class NBTSerializableCoder
{
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return ParametrizedCoder.handler(tools, ClassRef.NBT_SERIALIZABLE_INTERFACE, "fr.max2.annotated.lib.network.serializer.NBTSerializableSerializer",
			(fieldType, params) -> params.add(tools.naming.erasedType.get(fieldType) + "::new"),
			1,
			fieldType ->
			{
				SerializationCoder.requireDefaultConstructor(tools, fieldType);
				return fieldType;
			});
	}
}
