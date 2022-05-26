package fr.max2.annotated.processor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import fr.max2.annotated.processor.model.processor.IProcessor;
import fr.max2.annotated.processor.network.adapter.AdapterProcessor;
import fr.max2.annotated.processor.network.packet.PacketProcessor;
import fr.max2.annotated.processor.network.serializer.SerializationProcessor;
import fr.max2.annotated.processor.util.ProcessingTools;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AnnotatedProcessor extends AbstractProcessor
{
	// TODO [v3.0] Improve template system: use CodeSupplier / CodeConsumer
	// TODO [v3.0] Improve code generation: format resulting code : indentation, double empty lines, double spaces, ...
	// TODO [v3.0] Add SenderPlayer annotation

	private final List<IProcessor> processors = List.of(new AdapterProcessor(), new SerializationProcessor(), new PacketProcessor());
	private final Set<String> supportedAnnotations;

	public AnnotatedProcessor()
	{
		this.supportedAnnotations = this.processors.stream().flatMap(p -> p.getSupportedAnnotations().stream()).map(Class::getCanonicalName).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
        return this.supportedAnnotations;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);

		ProcessingTools tools = new ProcessingTools(this.processingEnv);
		for (var processor : this.processors)
		{
			processor.init(tools);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		for (var processor : this.processors)
		{
			processor.process(roundEnv);
		}

		return true;
	}
	//TODO [v2.1] Add code completion
}
