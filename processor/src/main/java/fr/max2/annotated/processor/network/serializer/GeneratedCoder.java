package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.element.TypeElement;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.processor.network.coder.handler.GeneratedHandler;
import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class GeneratedCoder
{
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools)
	{
		return new GeneratedHandler<>(tools, NetworkSerializable.class, type ->
		{
			TypeElement elem = tools.elements.asTypeElement(tools.types.asElement(type));
			NetworkSerializable annotation = elem.getAnnotation(NetworkSerializable.class);
			NetworkAdaptable adaptableAnnotation = elem.getAnnotation(NetworkAdaptable.class);
			if (adaptableAnnotation != null)
				throw new IncompatibleTypeException("The type '" + elem + "' has the " + NetworkAdaptable.class.getName() + " annnotation !");

			return new SimpleCoder(tools, type, SerializationProcessingUnit.getSerializerName(tools.naming.buildClassName(elem), annotation).qualifiedName() + ".INSTANCE");
		});
	}
}
