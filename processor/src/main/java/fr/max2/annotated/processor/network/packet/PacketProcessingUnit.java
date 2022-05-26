package fr.max2.annotated.processor.network.packet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.Packet;
import fr.max2.annotated.processor.model.ICodeSupplier;
import fr.max2.annotated.processor.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.network.adapter.AdapterProcessingUnit;
import fr.max2.annotated.processor.network.serializer.SerializationProcessingUnit;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingStatus;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.Visibility;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.RoundException;
import fr.max2.annotated.processor.util.template.TemplateHelper.ReplacementMap;

public class PacketProcessingUnit
{
	private final ProcessingTools tools;
    private final PacketProcessingContext context;
    public final ExecutableElement packetMethod;
    private final PacketDestination destination;
    public final Optional<? extends AnnotationMirror> annotation;
    private final Optional<? extends AnnotationMirror> adaptableAnnotation;
    private final Optional<? extends AnnotationMirror> serializableAnnotation;
    public final ClassName packetClassName;
	public final ClassName adapterClassName;
	public final ClassName adaptedClassName;

	public PacketProcessingUnit(ProcessingTools tools, PacketProcessingContext context, ExecutableElement packetMethod,
		Optional<? extends AnnotationMirror> annotation, Packet annotationData,
		Optional<? extends AnnotationMirror> adaptableAnnotation, NetworkAdaptable adaptableData,
		Optional<? extends AnnotationMirror> serializableAnnotation, NetworkSerializable serializableData)
	{
		this.tools = tools;
        this.context = context;
        this.packetMethod = packetMethod;
        this.destination = PacketDestination.fromAnnotationValue(annotationData.value());
        this.annotation = annotation;
        this.adaptableAnnotation = adaptableAnnotation;
        this.serializableAnnotation = serializableAnnotation;

		this.packetClassName = new ClassName(this.context.packetGroupClassName.packageName(), this.context.packetGroupClassName.shortName() + "." + packetMethod.getSimpleName());

		this.adapterClassName = AdapterProcessingUnit.getAdapterName(this.packetClassName, adaptableData);
		this.adaptedClassName = AdapterProcessingUnit.getAdaptedName(this.packetClassName, adaptableData);
	}

	public static void create(ProcessingTools tools, ExecutableElement method, Function<TypeElement, PacketProcessingContext> contextProvider) throws ProcessorException
	{
		Optional<? extends AnnotationMirror> annotation = tools.elements.getAnnotationMirror(method, Packet.class);
		Packet annotationData = method.getAnnotation(Packet.class);

        TypeElement enclosingClass = tools.elements.asTypeElement(method.getEnclosingElement());
        switch (enclosingClass.getNestingKind())
		{
		default:
		case ANONYMOUS:
		case LOCAL:
			throw ProcessorException.builder()
				.context(method, annotation)
				.build("Anonymous and local classes are not supported !");
		case MEMBER:
		case TOP_LEVEL:
			break;
		}

		if (Visibility.getTopLevelVisibility(method) != Visibility.PUBLIC)
		{
			throw ProcessorException.builder()
				.context(method, annotation)
				.build("Non-public methods are not supported !");
		}

        if (!method.getModifiers().contains(Modifier.STATIC))
        {
        	throw ProcessorException.builder()
				.context(method, annotation)
	            .build("The packet method must be static");
        }

        if (method.getReturnType().getKind() != TypeKind.VOID)
        {
        	throw ProcessorException.builder()
				.context(method, annotation)
	            .build("The return type of the packet method must be void");
        }

        PacketProcessingContext context = contextProvider.apply(enclosingClass);
        context.addPacket(method, annotation, annotationData);
	}

	public ProcessingStatus process(Consumer<ICodeSupplier> output)
	{
		try
		{
			output.accept(this.writePacket());
			return ProcessingStatus.SUCESSS;
		}
		catch (ProcessorException pe)
		{
			pe.log(this.tools);
		}
		catch (RoundException re)
		{
			return ProcessingStatus.DEFERRED;
		}
		catch (Exception e)
		{
			ProcessorException.builder()
				.context(this.packetMethod, this.annotation)
				.build("Unexpected exception generating the '" + this.packetClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e)
				.log(this.tools);
		}
		return ProcessingStatus.FAIL;
	}

	public ICodeSupplier writePacket() throws ProcessorException
	{
        List<? extends VariableElement> parameters = this.packetMethod.getParameters();
        List<? extends VariableElement> messageData = parameters.stream().filter(p -> !this.specialValue(p.asType()).isPresent()).collect(Collectors.toList());

		SimpleParameterListBuilder fields = new SimpleParameterListBuilder();
		SimpleParameterListBuilder fieldNames = new SimpleParameterListBuilder();
		boolean needsAdapter = this.adaptableAnnotation.isPresent();

		for (VariableElement data : messageData)
		{
			fields.add(this.tools.naming.typeUse.get(data.asType()) + " " + data.getSimpleName());
			fieldNames.add(data.getSimpleName().toString());

			if (!needsAdapter && AdapterProcessingUnit.needsAdapter(this.tools, data.asType()))
				needsAdapter = true;
		}

		SimpleParameterListBuilder methodParams = new SimpleParameterListBuilder();

		for  (VariableElement param : parameters)
		{
			methodParams.add(this.specialValue(param.asType()).orElse("msg." + param.getSimpleName().toString() + "()"));
		}

		SimpleCodeBuilder annotations = new SimpleCodeBuilder();

		this.serializableAnnotation.ifPresentOrElse(
			anno ->
			{
				annotations.write(anno.toString());
			},
			() ->
			{
				annotations.write("@");
				annotations.write(NetworkSerializable.class.getCanonicalName());
			});

		if (needsAdapter)
		{
			annotations.write(System.lineSeparator());
			this.adaptableAnnotation.ifPresentOrElse(
				anno ->
				{
					annotations.write(anno.toString());
				},
				() ->
				{
					annotations.write("@");
					annotations.write(NetworkAdaptable.class.getCanonicalName());
				});
		}

		ClassName serializerClassName = SerializationProcessingUnit.getSerializerName(needsAdapter ? this.adaptedClassName : this.packetClassName, this.packetMethod.getAnnotation(NetworkSerializable.class));

        String sheduled = this.tools.elements.getAnnotationValue(this.annotation, "runInMainThread").map(anno -> anno.getValue().toString()).orElse("true");

		ReplacementMap replacements = new ReplacementMap();
		replacements.putString("packetName", this.packetMethod.getSimpleName());
		replacements.putString("targetName", this.context.enclosingClassName.qualifiedName());
		replacements.putString("function", this.packetMethod.getSimpleName());
		replacements.putCode("parameters", methodParams);
		replacements.putCode("packetParameters", fields);
		replacements.putCode("packetParameterNames", fieldNames);
		replacements.putString("adapter", needsAdapter ? this.adapterClassName.qualifiedName() + ".INSTANCE" : "");
		replacements.putString("dataClassName", needsAdapter ? this.adaptedClassName.qualifiedName() : this.packetMethod.getSimpleName());
		replacements.putString("serializer", serializerClassName.qualifiedName() + ".INSTANCE");
        replacements.putBoolean("serverPacket", this.destination.isServer());
        replacements.putBoolean("clientPacket", this.destination.isClient());
		replacements.putString("sheduled", sheduled);
		replacements.putCode("annotations", annotations);

		return this.tools.templates.readWithLog("templates/TemplatePacket.jvtp", replacements, this.packetMethod, this.annotation);
	}

    private Optional<String> specialValue(TypeMirror type)
    {
        TypeElement elem = this.tools.elements.asTypeElement(this.tools.types.asElement(type));
        if (elem != null)
        {
            if (elem.getQualifiedName().contentEquals(ClassRef.FORGE_NETWORK_CONTEXT))
                return Optional.of("ctxSup.get()");

            if (elem.getQualifiedName().contentEquals(ClassRef.SERVER_PLAYER))
                return Optional.of("ctxSup.get().getSender()");
        }

        return Optional.empty();
    }
}
