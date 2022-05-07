package fr.max2.annotated.processor.network.packet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import fr.max2.annotated.api.network.ClientPacket;
import fr.max2.annotated.api.network.ServerPacket;
import fr.max2.annotated.processor.model.processor.UnitProcessor;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class PacketProcessor extends UnitProcessor<PacketProcessingContext>
{

	public PacketProcessor()
	{
		super(ClientPacket.class, ServerPacket.class);
	}

	@Override
	protected Collection<PacketProcessingContext> buildProcessingUnits(RoundEnvironment roundEnv)
	{
	    Map<TypeElement, PacketProcessingContext> contexts = new HashMap<>();

	    for (var context : this.deferredUnits)
	    {
	    	contexts.put(context.enclosingClass, context);
	    }

	    for (PacketDestination dest : PacketDestination.values())
	    {
	        for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(dest.getAnnotationClass())))
	        {
				try
				{
					PacketProcessingUnit.create(this.tools, method, dest, type -> contexts.computeIfAbsent(type, clazz -> new PacketProcessingContext(this.tools, clazz)));
				}
				catch (ProcessorException e)
				{
					e.log(this.tools);
					continue;
				}
	        }
	    }

	    return contexts.values();
	}

	@Override
	protected void onFailedToDeferProcess(PacketProcessingContext unit)
	{
		ProcessorException.builder()
			.context(unit.enclosingClass)
			.build("Could not process serializable type !")
			.log(this.tools);
	}
}
