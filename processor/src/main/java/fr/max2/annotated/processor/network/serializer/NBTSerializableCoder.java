package fr.max2.annotated.processor.network.serializer;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;

public class NBTSerializableCoder
{
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return GenericCoder.handler(tools, ClassRef.NBT_SERIALIZABLE_INTERFACE, "fr.max2.annotated.lib.network.serializer.NBTSerializableSerializer",
			(fieldType, builder) ->
			{
				tools.types.requireConcreteType(fieldType);
				tools.types.requireDefaultConstructor(fieldType);
				
				builder.add(tools.naming.erasedType.get(fieldType) + "::new");
				builder.addCoders(1, fieldType);
			});
	}
}
