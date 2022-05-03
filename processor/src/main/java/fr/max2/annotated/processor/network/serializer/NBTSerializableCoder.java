package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.CoderCompatibility;
import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.network.serializer.GenericCoder.Builder;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;

public class NBTSerializableCoder
{

	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		DeclaredType entityType = tools.types.asDeclared(tools.types.erasure(tools.elements.getTypeElement(ClassRef.ENTITY_BASE).asType()));
		DeclaredType interfaceType = tools.types.asDeclared(tools.types.erasure(tools.elements.getTypeElement(ClassRef.NBT_SERIALIZABLE_INTERFACE).asType()));
		return new TypedDataHandler<>(tools, interfaceType, true, fieldType ->
		{
			tools.types.requireConcreteType(fieldType);
			tools.types.requireDefaultConstructor(fieldType);

			Builder builder = GenericCoder.builder(tools, interfaceType, fieldType);
			builder.add(tools.naming.erasedType.get(fieldType) + "::new");
			builder.addCoders(1, fieldType);
			return builder.build("fr.max2.annotated.lib.network.serializer.NBTSerializableSerializer");
		})
		{
			@Override
			public CoderCompatibility getCompatibilityFor(TypeMirror type)
			{
				if (this.tools.types.isAssignable(type, entityType))
					return CoderCompatibility.INCOMPATIBLE;

				return super.getCompatibilityFor(type);
			}
		};
	}
}
