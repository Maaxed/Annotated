package fr.max2.annotated.processor.network.adapter;

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

public class AdapterProcessor
{
	private ProcessingTools tools;
	private final Set<ClassName> processedClasses = new HashSet<>();
	private final Collection<AdapterProcessingUnit> deferredUnits = new ArrayList<>();

	public AdapterProcessor(ProcessingTools tools)
	{
		this.tools = tools;
	}

	public void process(RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
		{
			for (AdapterProcessingUnit unit : this.deferredUnits)
			{
				ProcessorException.builder()
					.context(unit.adaptableClass, unit.annotation)
					.build("Could not process adaptable type !")
					.log(this.tools);
			}
			return;
		}

		Collection<AdapterProcessingUnit> units;
		try
		{
			units = this.buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return;
		}

		for (AdapterProcessingUnit unit : units)
		{
			if (this.processedClasses.contains(unit.adaptableClassName))
				continue; // Skip the class if it has already been processed in a previous round

			unit.process();

			switch (unit.getStatus())
			{
			case SUCESSS:
				this.processedClasses.add(unit.adaptableClassName);
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

	private Collection<AdapterProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		List<AdapterProcessingUnit> units = new ArrayList<>(this.deferredUnits);
		this.deferredUnits.clear();

		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkAdaptable.class)))
		{
			Optional<? extends AnnotationMirror> annotation = this.tools.elements.getAnnotationMirror(type, NetworkAdaptable.class);
			Optional<? extends AnnotationMirror> serailizableAnno = this.tools.elements.getAnnotationMirror(type, NetworkSerializable.class);
			if (serailizableAnno.isEmpty())
			{
				ProcessorException.builder()
					.context(type, annotation)
					.build("Classes annotated with the " + NetworkAdaptable.class.getName() + " annotaiton should also have the " + NetworkSerializable.class.getName() + " annotaiton !")
					.log(this.tools);
				continue; // Skip this class
			}

			switch (type.getNestingKind())
			{
			default:
			case ANONYMOUS:
			case LOCAL:
				ProcessorException.builder()
					.context(type, annotation)
					.build("Anonymous and local classes are not supported !")
					.log(this.tools);
				continue; // Skip this class
			case MEMBER:
				if (!type.getModifiers().contains(Modifier.STATIC))
				{
					ProcessorException.builder()
						.context(type, annotation)
						.build("Non-static nested classes are not supported !")
						.log(this.tools);
					continue; // Skip this class
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
				continue; // Skip this class
			}

			units.add(new AdapterProcessingUnit(this.tools, type, annotation, type.getAnnotation(NetworkAdaptable.class), serailizableAnno, type.getAnnotation(NetworkSerializable.class)));
		}

		return units;
	}
}
