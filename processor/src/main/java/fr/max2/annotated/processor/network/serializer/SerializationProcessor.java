package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.Visibility;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class SerializationProcessor
{
	private ProcessingTools tools;
	private Set<ClassName> processedClasses = new HashSet<>();

	public SerializationProcessor(ProcessingTools tools)
	{
		this.tools = tools;
	}
	
	public void process(RoundEnvironment roundEnv)
	{
		Collection<SerializationProcessingUnit> contexts;
		try
		{
			contexts = buildSerializationProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return;
		}
		
		for (SerializationProcessingUnit context : contexts)
		{
			if (this.processedClasses.contains(context.serializableClassName))
				continue; // Skip the class if it has already been processed in a previous round
			
			context.process();
			
			if (!context.hasErrors())
				this.processedClasses.add(context.serializableClassName);
		}
	}
	
	private Collection<SerializationProcessingUnit> buildSerializationProcessingUnits(RoundEnvironment roundEnv)
	{
		List<SerializationProcessingUnit> contexts = new ArrayList<>();
		
		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkSerializable.class)))
		{
			Optional<? extends AnnotationMirror> annotation = this.tools.elements.getAnnotationMirror(type, NetworkSerializable.class.getCanonicalName());
			switch (type.getNestingKind())
			{
			default:
			case ANONYMOUS:
			case LOCAL:
				ProcessorException.builder()
					.context(type, annotation)
					.build("Anonymous and local classes are not supported !")
					.log(this.tools);
				continue; // Skip this packet
			case MEMBER:
				TypeElement elem = type;
				while (elem != null && elem.getNestingKind() != NestingKind.TOP_LEVEL)
				{
					Element enclosing = elem.getEnclosingElement();
					if (enclosing != null && !enclosing.getKind().isInterface())
					{
						if (!type.getModifiers().contains(Modifier.STATIC))
						{
							ProcessorException.builder()
								.context(type, annotation)
								.build("Non-static nested classes are not supported !")
								.log(this.tools);
							continue; // Skip this packet
						}
					}
					elem = this.tools.elements.asTypeElement(enclosing);
				}
				break;
			case TOP_LEVEL:
				break;
			}

			if (Visibility.getTopLevelVisibility(type) != Visibility.PUBLIC)
			{
				ProcessorException.builder()
					.context(type, annotation)
					.build("Non-public classes are not supported !")
					.log(this.tools);
				continue; // Skip this packet
			}
			
			contexts.add(new SerializationProcessingUnit(this.tools, type, annotation, type.getAnnotation(NetworkSerializable.class)));
		}
		
		return contexts;
	}
}
