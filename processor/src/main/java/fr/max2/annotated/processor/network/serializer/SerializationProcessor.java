package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.Visibility;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class SerializationProcessor
{
	private ProcessingTools tools;
	private Set<ClassName> processedClasses = new HashSet<>();
	private final Collection<SerializationProcessingUnit> deferredUnits = new ArrayList<>();

	public SerializationProcessor(ProcessingTools tools)
	{
		this.tools = tools;
	}

	public void process(RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
		{
			for (SerializationProcessingUnit unit : this.deferredUnits)
			{
				ProcessorException.builder()
					.context(unit.serializableClass, unit.annotation)
					.build("Could not process serializable type !")
					.log(this.tools);
			}
			return;
		}

		Collection<SerializationProcessingUnit> units;
		try
		{
			units = this.buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return;
		}

		for (SerializationProcessingUnit unit : units)
		{
			if (this.processedClasses.contains(unit.serializableClassName))
				continue; // Skip the class if it has already been processed in a previous round

			unit.process();


			switch (unit.getStatus())
			{
			case SUCESSS:
				this.processedClasses.add(unit.serializableClassName);
				break;
			case DEFERRED:
				this.deferredUnits.add(unit);
				break;
			default:
			case FAIL:
				break;
			}
		}
	}

	private Collection<SerializationProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		List<SerializationProcessingUnit> units = new ArrayList<>(this.deferredUnits);
		this.deferredUnits.clear();

		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkSerializable.class)))
		{
			Optional<? extends AnnotationMirror> annotation = this.tools.elements.getAnnotationMirror(type, NetworkSerializable.class);
			Optional<? extends AnnotationMirror> adaptableAnno = this.tools.elements.getAnnotationMirror(type, NetworkAdaptable.class);
			if (adaptableAnno.isPresent())
				continue; // Skip this class

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
				if (!type.getModifiers().contains(Modifier.STATIC))
				{
					ProcessorException.builder()
						.context(type, annotation)
						.build("Non-static nested classes are not supported !")
						.log(this.tools);
					continue; // Skip this packet
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

			units.add(new SerializationProcessingUnit(this.tools, type, annotation, type.getAnnotation(NetworkSerializable.class)));
		}

		return units;
	}
}
