package fr.max2.annotated.processor.network.serializer;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class MapCoder
{
	private static final String MAP_TYPE = Map.class.getCanonicalName();

	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return ParametrizedCoder.handler(tools, MAP_TYPE, "fr.max2.annotated.lib.network.serializer.CollectionSerializer",
			(fieldType, params) -> params.add(tools.naming.erasedType.get(fieldType) + "::new"),
			fieldType ->
			{
				TypeElement mapElem = tools.elements.getTypeElement(MAP_TYPE);
				TypeMirror mapType = mapElem.asType();
				DeclaredType refinedType = tools.types.refineTo(fieldType, mapType);
				if (refinedType == null) throw new IncompatibleTypeException("The type '" + fieldType + "' is not a sub type of " + MAP_TYPE);
				
				TypeMirror keyExtType = refinedType.getTypeArguments().get(0);
				SerializationCoder keyCoder = tools.coders.getCoder(keyExtType);
				TypeMirror keyType = keyExtType;
				if (keyType.getKind().isPrimitive()) keyType = tools.types.boxedClass(tools.types.asPrimitive(keyType)).asType();
				TypeMirror keyIntType = keyType;
				
				TypeMirror valueExtType = refinedType.getTypeArguments().get(1);
				SerializationCoder valueCoder = tools.coders.getCoder(valueExtType);
				TypeMirror valueType = valueExtType;
				if (valueType.getKind().isPrimitive()) valueType = tools.types.boxedClass(tools.types.asPrimitive(valueType)).asType();
				TypeMirror valueIntType = valueType;
				
				DeclaredType erasureIntType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType) fieldType, keyExtType, tools.types.shallowErasure(keyIntType)), valueExtType, tools.types.shallowErasure(valueIntType));
				TypeMirror implType = fieldType;
				
				String implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(fieldType)));
				
				if (!implName.isEmpty())
				{
					TypeElement elem = tools.elements.getTypeElement(implName);
					if (elem == null) throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + MAP_TYPE);
					
					implType = elem.asType();
					DeclaredType refinedImpl = tools.types.refineTo(implType, mapType);
					if (refinedImpl == null) throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + MAP_TYPE);
					
					TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
					TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
					DeclaredType revisedImplType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType) implType, implKeyFullType, tools.types.shallowErasure(keyIntType)), implValueFullType, tools.types.shallowErasure(valueIntType));
					if (!tools.types.isAssignable(revisedImplType, erasureIntType)) throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + fieldType + "'");
				}
				
				SerializationCoder.requireDefaultConstructor(tools.types, implType, null);
				
				return List.of(keyCoder, valueCoder);
			});
	}
	
	private static String defaultImplementation(TypeElement type)
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
