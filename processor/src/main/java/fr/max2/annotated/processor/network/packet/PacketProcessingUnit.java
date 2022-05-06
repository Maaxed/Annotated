package fr.max2.annotated.processor.network.packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.RoundException;

public class PacketProcessingUnit
{
	private final ProcessingTools tools;
    private final PacketProcessingContext context;
    public final ExecutableElement packetMethod;
    private final PacketDirection side;
    public final Optional<? extends AnnotationMirror> annotation;
    private final Optional<? extends AnnotationMirror> adaptableAnnotation;
    private final Optional<? extends AnnotationMirror> serializableAnnotation;
    public final ClassName packetClassName;
	public final ClassName adapterClassName;
	public final ClassName adaptedClassName;
	private ProcessingStatus status = ProcessingStatus.SUCESSS;

	public PacketProcessingUnit(ProcessingTools tools, PacketProcessingContext context, ExecutableElement packetMethod, PacketDirection side,
		Optional<? extends AnnotationMirror> annotation,
		Optional<? extends AnnotationMirror> adaptableAnnotation, NetworkAdaptable adaptableData,
		Optional<? extends AnnotationMirror> serializableAnnotation, NetworkSerializable serializableData)
	{
		this.tools = tools;
        this.context = context;
        this.packetMethod = packetMethod;
        this.side = side;
        this.annotation = annotation;
        this.adaptableAnnotation = adaptableAnnotation;
        this.serializableAnnotation = serializableAnnotation;

		this.packetClassName = getPacketName(this.context.enclosingClassName, this.context.enclosingClassName.shortName().replace('.', '_') + "_" + packetMethod.getSimpleName());

		this.adapterClassName = AdapterProcessingUnit.getAdapterName(this.packetClassName, adaptableData);
		this.adaptedClassName = AdapterProcessingUnit.getAdaptedName(this.packetClassName, adaptableData);
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
        replacements.put("serverPacket", Boolean.toString(this.side.isServer()));
        replacements.put("clientPacket", Boolean.toString(this.side.isClient()));
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
