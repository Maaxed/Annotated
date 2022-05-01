package fr.max2.annotated.processor.network.adapter;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class MapCoder
{
	private static final String MAP_TYPE = Map.class.getCanonicalName();

	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools)
	{
		return GenericCoder.handler(tools, MAP_TYPE, MAP_TYPE, "fr.max2.annotated.lib.network.adapter.MapAdapterr",
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

				builder.addCoders(2, impl, impl);
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
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + MAP_TYPE);

			implType = elem.asType();
		}

		return implType;
	}

	private static String defaultImplementationName(TypeElement type)
	{
		switch (type.getQualifiedName().toString())
		{
		case "java.util.Map":
			return "java.util.HashMap";
		default:
			return "";
		}
	}
}
