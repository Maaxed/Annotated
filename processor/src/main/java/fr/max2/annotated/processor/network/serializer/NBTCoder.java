package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.CoderCompatibility;
import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.serializer.GenericCoder.Builder;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;

public class NBTCoder
{
	public static ICoderHandler<SerializationCoder> abstractHandler(ProcessingTools tools)
	{
		return SimpleCoder.handler(tools, ClassRef.NBT_BASE, "fr.max2.annotated.lib.network.serializer.TagSerializer.Abstract");
	}
	
	public static ICoderHandler<SerializationCoder> concreteHandler(ProcessingTools tools)
	{
		DeclaredType collectionType = tools.types.asDeclared(tools.elements.getTypeElement(ClassRef.NBT_BASE).asType());
		return new NamedDataHandler<>(tools, ClassRef.NBT_BASE, true, fieldType ->
		{
			Builder builder = GenericCoder.builder(tools, collectionType, fieldType);
			builder.add(tools.naming.erasedType.get(fieldType) + ".TYPE");
			return builder.build("fr.max2.annotated.lib.network.serializer.TagSerializer.Concrete");
		})
		{
			@Override
			public CoderCompatibility getCompatibilityFor(TypeMirror type)
			{
				switch (super.getCompatibilityFor(type))
				{
				default:
				case INCOMPATIBLE:
				case EXACT_MATCH:
					return CoderCompatibility.INCOMPATIBLE;
				case SUPER_TYPE_MATCH:
					Element elem = this.tools.types.asElement(type);
					if (elem == null)
						return CoderCompatibility.INCOMPATIBLE;
					
					if (elem.getKind() == ElementKind.INTERFACE || elem.getModifiers().contains(Modifier.ABSTRACT))
						return CoderCompatibility.INCOMPATIBLE;
					return CoderCompatibility.EXACT_MATCH;
				}
			}		
		};
	}
}
