package fr.max2.annotated.processor.network.packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	private ProcessingStatus status = ProcessingStatus.SUCESSS;

	public PacketProcessingUnit(ProcessingTools tools, PacketProcessingContext context, ExecutableElement packetMethod, PacketDestination dest,
		Optional<? extends AnnotationMirror> annotation,
		Optional<? extends AnnotationMirror> adaptableAnnotation, NetworkAdaptable adaptableData,
		Optional<? extends AnnotationMirror> serializableAnnotation, NetworkSerializable serializableData)
	{
		this.tools = tools;
        this.context = context;
        this.packetMethod = packetMethod;
        this.destination = dest;
        this.annotation = annotation;
        this.adaptableAnnotation = adaptableAnnotation;
        this.serializableAnnotation = serializableAnnotation;

		this.packetClassName = getPacketName(this.context.enclosingClassName, this.context.enclosingClassName.shortName().replace('.', '_') + "_" + packetMethod.getSimpleName());

		this.adapterClassName = AdapterProcessingUnit.getAdapterName(this.packetClassName, adaptableData);
		this.adaptedClassName = AdapterProcessingUnit.getAdaptedName(this.packetClassName, adaptableData);
	}

	public static void create(ProcessingTools tools, ExecutableElement method, PacketDestination dest, Function<TypeElement, PacketProcessingContext> contextProvider) throws ProcessorException
	{
		Optional<? extends AnnotationMirror> annotation = tools.elements.getAnnotationMirror(method, dest.getAnnotationClass());

        if (method.getAnnotation(dest.opposite().getAnnotationClass()) != null)
        {
			throw ProcessorException.builder()
				.context(method, annotation)
				.build("A packet cannot be used in both directions !");
        }

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
        context.addPacket(method, dest, annotation);
	}

	public static ClassName getPacketName(ClassName enclosingClassName, String userDefinedName)
	{
		String className = userDefinedName;

		int sep = className.lastIndexOf('.');

		String packageName = enclosingClassName.packageName();

		if (sep != -1)
		{
			className = className.substring(sep + 1);
			packageName = className.substring(0, sep);
		}
		else if (className.isEmpty())
		{
			className = enclosingClassName.shortName().replace('.', '_') + "_Packets";
		}

		return new ClassName(packageName, className);
	}

	public ProcessingStatus getStatus()
	{
		return this.status;
	}

	public void process()
	{
		try
        {
			this.writePacket();
			this.status = ProcessingStatus.SUCESSS;
			return;
        }
		catch (ProcessorException pe)
		{
			pe.log(this.tools);
		}
		catch (RoundException re)
		{
			this.status = ProcessingStatus.DEFERRED;
		}
		catch (Exception e)
		{
			ProcessorException.builder()
				.context(this.packetMethod, this.annotation)
				.build("Unexpected exception generating the '" + this.packetClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e)
				.log(this.tools);
		}
		this.status = ProcessingStatus.FAIL;
	}

	private void writePacket() throws ProcessorException
	{
        List<? extends VariableElement> parameters = this.packetMethod.getParameters();
        List<? extends VariableElement> messageData = parameters.stream().filter(p -> !this.specialValue(p.asType()).isPresent()).collect(Collectors.toList());

		SimpleParameterListBuilder fields = new SimpleParameterListBuilder();
		boolean needsAdapter = this.adaptableAnnotation.isPresent();

		for (VariableElement data : messageData)
		{
			fields.add(this.tools.naming.typeUse.get(data.asType()) + " " + data.getSimpleName());

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

		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.packetClassName.packageName());
		replacements.put("packetName", this.packetClassName.shortName());
		replacements.put("targetName", this.context.enclosingClassName.qualifiedName());
		replacements.put("function", this.packetMethod.getSimpleName().toString());
		replacements.put("parameters", methodParams.buildMultiLines());
		replacements.put("fieldDeclaration", fields.buildMultiLines());
		replacements.put("adapter", needsAdapter ? this.adapterClassName.qualifiedName() + ".INSTANCE" : "");
		replacements.put("dataClassName", needsAdapter ? this.adaptedClassName.qualifiedName() : this.packetClassName.shortName());
		replacements.put("serializer", serializerClassName.qualifiedName() + ".INSTANCE");
        replacements.put("serverPacket", Boolean.toString(this.destination.isServer()));
        replacements.put("clientPacket", Boolean.toString(this.destination.isClient()));
		replacements.put("sheduled", sheduled);
		replacements.put("annotations", annotations.build());

		this.tools.templates.writeFileWithLog(this.packetClassName.qualifiedName(), "templates/TemplatePacket.jvtp", replacements, this.packetMethod, this.annotation, this.packetMethod);
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
