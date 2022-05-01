package fr.max2.annotated.processor.network.adapter;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class CollectionCoder
{
	private static final String COLLECTION_TYPE = Collection.class.getCanonicalName();

	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools)
	{
		return GenericCoder.handler(tools, COLLECTION_TYPE, COLLECTION_TYPE, "fr.max2.annotated.lib.network.adapter.CollectionAdapter",
			(fieldType, builder) ->
			{
				TypeMirror impl = defaultImplementation(tools, fieldType);

				String constructor;
				tools.types.requireConcreteType(impl);
				if (tools.types.findConstructor(impl, List.of(tools.types.getPrimitiveType(TypeKind.INT))) != null)
				{
					constructor = tools.naming.erasedType.get(impl) + "::new";
				}
				else
				{
					tools.types.requireDefaultConstructor(impl);
					constructor = "size -> new " + tools.naming.erasedType.get(impl) + "()";
				}
				builder.addAll(constructor, constructor);

				builder.addCoders(1, impl, impl);
			});
	}

	private static TypeMirror defaultImplementation(ProcessingTools tools, TypeMirror fieldType) throws CoderException
	{
		TypeMirror implType = fieldType;

		String implName = defaultImplementationName(tools.elements.asTypeElement(tools.types.asElement(fieldType)));

		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + COLLECTION_TYPE);

			implType = elem.asType();
		}

		return implType;
	}

	private static String defaultImplementationName(TypeElement type)
	{
		switch (type.getQualifiedName().toString())
		{
		case "java.util.Collection":
		case "java.util.List":
			return "java.util.ArrayList";
		case "java.util.Set":
			return "java.util.HashSet";
		default:
			return "";
		}
	}
}
