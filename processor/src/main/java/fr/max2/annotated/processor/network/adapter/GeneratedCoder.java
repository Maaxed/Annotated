package fr.max2.annotated.processor.network.adapter;

import javax.lang.model.element.TypeElement;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.processor.network.coder.handler.GeneratedHandler;
import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.RoundException;

public class GeneratedCoder
{
	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools)
	{
		return new GeneratedHandler<>(tools, NetworkAdaptable.class, type ->
		{
			TypeElement elem = tools.elements.asTypeElement(tools.types.asElement(type));
			NetworkAdaptable annotation = elem.getAnnotation(NetworkAdaptable.class);
			ClassName className = tools.naming.buildClassName(elem);
			ClassName adaptedName = AdapterProcessingUnit.getAdaptedName(className, annotation);
			TypeElement adaptedElem = tools.elements.getTypeElement(adaptedName.qualifiedName());
			if (adaptedElem == null)
				throw new RoundException("The element '" + elem + "' has not been processed yet !");

			return new SimpleCoder(tools, type, adaptedElem.asType(), AdapterProcessingUnit.getAdapterName(className, annotation).qualifiedName() + ".INSTANCE");
		});
	}
}
