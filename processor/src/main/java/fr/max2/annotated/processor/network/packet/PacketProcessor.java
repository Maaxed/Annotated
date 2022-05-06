package fr.max2.annotated.processor.network.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.Visibility;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class PacketProcessor
{
	private ProcessingTools tools;
	private Set<ClassName> processedClasses = new HashSet<>();
	private final Collection<PacketProcessingContext> deferredUnits = new ArrayList<>();

	public PacketProcessor(ProcessingTools tools)
	{
		this.tools = tools;
	}

	public void process(RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
		{
			for (PacketProcessingContext unit : this.deferredUnits)
			{
				ProcessorException.builder()
					.context(unit.enclosingClass)
					.build("Could not process serializable type !")
					.log(this.tools);
			}
			return;
		}

		Collection<PacketProcessingContext> contexts;
		try
		{
			contexts = this.buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return;
		}

		for (PacketProcessingContext context : contexts)
		{
			if (this.processedClasses.contains(context.enclosingClassName))
				continue; // Skip the class if it has already been processed in a previous round

		    context.process();

		    switch (context.getStatus())
			{
			case SUCESSS:
				this.processedClasses.add(context.enclosingClassName);
				break;
			case DEFERRED:
				this.deferredUnits.add(context);
				break;
			default:
			case FAIL:
				break;
			}
		}
	}

	private Collection<PacketProcessingContext> buildProcessingUnits(RoundEnvironment roundEnv)
	{
	    Map<TypeElement, PacketProcessingContext> contexts = new HashMap<>();

	    for (var context : this.deferredUnits)
	    {
	    	contexts.put(context.enclosingClass, context);
	    }

	    for (PacketDirection dir : PacketDirection.values())
	    {
	        for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(dir.getAnnotationClass())))
	        {
	            Optional<? extends AnnotationMirror> annotation = this.tools.elements.getAnnotationMirror(method, dir.getAnnotationClass());

	            if (method.getAnnotation(dir.opposite().getAnnotationClass()) != null)
	            {
					ProcessorException.builder()
						.context(method, annotation)
						.build("A packet cannot be used in both directions !")
						.log(this.tools);
	                continue; // Skip this packet
	            }

	            TypeElement enclosingClass = this.tools.elements.asTypeElement(method.getEnclosingElement());
	            switch (enclosingClass.getNestingKind())
				{
				default:
				case ANONYMOUS:
				case LOCAL:
					ProcessorException.builder()
						.context(method, annotation)
						.build("Anonymous and local classes are not supported !")
						.log(this.tools);
					continue; // Skip this packet
				case MEMBER:
				case TOP_LEVEL:
					break;
				}

				if (Visibility.getTopLevelVisibility(method) != Visibility.PUBLIC)
				{
					ProcessorException.builder()
						.context(method, annotation)
						.build("Non-public methods are not supported !")
						.log(this.tools);
					continue; // Skip this packet
				}

		        if (!method.getModifiers().contains(Modifier.STATIC))
		        {
					ProcessorException.builder()
						.context(method, annotation)
			            .build("The packet method must be static")
			            .log(this.tools);
					continue; // Skip this packet
		        }

		        if (method.getReturnType().getKind() != TypeKind.VOID)
		        {
					ProcessorException.builder()
						.context(method, annotation)
			            .build("The return type of the packet method must be void")
			            .log(this.tools);
					continue; // Skip this packet
		        }

	            PacketProcessingContext context = contexts.computeIfAbsent(enclosingClass, clazz -> new PacketProcessingContext(this.tools, clazz));
	            context.addPacket(method, dir, annotation);
	        }
	    }

	    return contexts.values();
	}
}
