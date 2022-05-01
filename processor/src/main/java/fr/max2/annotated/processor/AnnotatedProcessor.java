package fr.max2.annotated.processor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import fr.max2.annotated.api.network.ClientPacket;
import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.ServerPacket;
import fr.max2.annotated.processor.network.adapter.AdapterProcessor;
import fr.max2.annotated.processor.network.serializer.SerializationProcessor;
import fr.max2.annotated.processor.util.ProcessingTools;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AnnotatedProcessor extends AbstractProcessor
{
	private static final Set<String> SUPPORTED_ANNOTATIONS = Stream.of(
			ClientPacket.class, ServerPacket.class, NetworkSerializable.class, NetworkAdaptable.class
		).map(Class::getCanonicalName).collect(Collectors.toUnmodifiableSet());

	private ProcessingTools tools;
	private AdapterProcessor adapter;
	private SerializationProcessor serialization;

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
		this.adapter = new AdapterProcessor(this.tools);
		this.serialization = new SerializationProcessor(this.tools);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		this.adapter.process(roundEnv);
		this.serialization.process(roundEnv);

		return true;
	}
	//TODO [v2.1] Add code completion
}
