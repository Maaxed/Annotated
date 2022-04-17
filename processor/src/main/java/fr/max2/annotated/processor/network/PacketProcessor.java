package fr.max2.annotated.processor.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.network.ClientPacket;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.ServerPacket;
import fr.max2.annotated.processor.network.serializer.SerializationProcessingUnit;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class PacketProcessor extends AbstractProcessor
{
	private static final Set<String> SUPPORTED_ANNOTATIONS = Stream.of(
			ClientPacket.class, ServerPacket.class, NetworkSerializable.class
		).map(Class::getCanonicalName).collect(Collectors.toUnmodifiableSet());
	
	private ProcessingTools tools;
	private Set<ClassName> processedClasses = new HashSet<>();
	
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
        return SUPPORTED_ANNOTATIONS;
	}
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.tools = new ProcessingTools(this.processingEnv);
		this.processedClasses.clear();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
			return false;
		
		
		/*Collection<PacketProcessingContext> contexts;
		try
		{
			contexts = buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return true;
		}
		
		for (PacketProcessingContext context : contexts)
		{
			if (this.processedClasses.contains(context.enclosingClassName))
				continue; // Skip the class if it has already been processed in a previous round
			
			context.processPackets();
			
			if (!context.hasErrors())
				this.processedClasses.add(context.enclosingClassName);
		}*/
		
		Collection<SerializationProcessingUnit> contexts;
		try
		{
			contexts = buildSerializationProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return true;
		}
		
		for (SerializationProcessingUnit context : contexts)
		{
			if (this.processedClasses.contains(context.serializableClassName))
				continue; // Skip the class if it has already been processed in a previous round
			
			context.process();
			
			if (!context.hasErrors())
				this.processedClasses.add(context.serializableClassName);
		}
		
		return true;
	}
	
	/*private Collection<PacketProcessingContext> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		Map<TypeElement, PacketProcessingContext> contexts = new HashMap<>();
		
		for (PacketDirection dir : PacketDirection.values())
		{
			for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(dir.getAnnotationClass())))
			{
				TypeElement enclosingClass = this.tools.elements.asTypeElement(method.getEnclosingElement());
				if (enclosingClass.getNestingKind().isNested())
				{
					this.tools.log(Kind.ERROR, "Nested / anonymous classes are not supported !", method, this.tools.elements.getAnnotationMirror(method, dir.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				
				if (method.getAnnotation(dir.opposite().getAnnotationClass()) != null)
				{
					this.tools.log(Kind.ERROR, "A packet cannot be used in both directions !", method, this.tools.elements.getAnnotationMirror(method, dir.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				
				PacketProcessingContext networkUnit = contexts.computeIfAbsent(enclosingClass, clazz -> new PacketProcessingContext(this.tools, clazz));
				networkUnit.addPacket(method, dir);
			}
		}
		
		return contexts.values();
	}*/
	
	private Collection<SerializationProcessingUnit> buildSerializationProcessingUnits(RoundEnvironment roundEnv)
	{
		List<SerializationProcessingUnit> contexts = new ArrayList<>();
		
		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkSerializable.class)))
		{
			Optional<? extends AnnotationMirror> annotation = this.tools.elements.getAnnotationMirror(type, NetworkSerializable.class.getCanonicalName());
			if (type.getNestingKind().isNested())
			{
				ProcessorException.builder()
					.context(type, annotation)
					.build("Nested / anonymous classes are not supported !")
					.log(this.tools);
				continue; // Skip this packet
			}
			
			contexts.add(new SerializationProcessingUnit(this.tools, type, annotation));
		}
		
		return contexts;
	}
	//TODO [v2.1] Add code completion
}
