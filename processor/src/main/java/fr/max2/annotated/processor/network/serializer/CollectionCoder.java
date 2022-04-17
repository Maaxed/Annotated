package fr.max2.annotated.processor.network.serializer;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class CollectionCoder
{
	private static final String COLLECTION_TYPE = Collection.class.getCanonicalName();

	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return ParametrizedCoder.handler(tools, COLLECTION_TYPE, "fr.max2.annotated.lib.network.serializer.CollectionSerializer",
			(fieldType, params) -> params.add(tools.naming.erasedType.get(fieldType) + "::new"),
			fieldType ->
			{
				TypeMirror collectionType = tools.elements.getTypeElement(COLLECTION_TYPE).asType();
				DeclaredType refinedType = tools.types.refineTo(fieldType, collectionType);
				if (refinedType == null) throw new IncompatibleTypeException("The type '" + fieldType + "' is not a sub type of " + COLLECTION_TYPE);
				
				TypeMirror contentFullType = refinedType.getTypeArguments().get(0);
				SerializationCoder contentCoder = tools.coders.getCoder(contentFullType);
				TypeMirror contentType = contentFullType;
				if (contentType.getKind().isPrimitive())
					contentType = tools.types.boxedClass(tools.types.asPrimitive(contentType)).asType();
				
				contentType = tools.types.shallowErasure(contentType);
				
				DeclaredType codedType = tools.types.replaceTypeArgument((DeclaredType) fieldType, contentFullType, contentType);
				TypeMirror implType = fieldType;
				
				String implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(fieldType)));
				
				if (!implName.isEmpty())
				{
					TypeElement elem = tools.elements.getTypeElement(implName);
					if (elem == null) throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + COLLECTION_TYPE);
					
					implType = elem.asType();
					DeclaredType refinedImpl = tools.types.refineTo(implType, collectionType);
					if (refinedImpl == null) throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + COLLECTION_TYPE);
					
					TypeMirror implContentType = refinedImpl.getTypeArguments().get(0);
					DeclaredType revisedImplType = tools.types.replaceTypeArgument((DeclaredType) implType, implContentType, contentType);
					if (!tools.types.isAssignable(revisedImplType, codedType)) throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + fieldType + "'");
				}
				
				SerializationCoder.requireDefaultConstructor(tools.types, implType, null);
				
				return List.of(contentCoder);
			});
	}
	
	private static String defaultImplementation(TypeElement type)
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
