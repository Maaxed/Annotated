package fr.max2.annotated.processor.network.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.processor.model.processor.IProcessingUnit;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingStatus;
import fr.max2.annotated.processor.util.ProcessingTools;

public class PacketProcessingContext implements IProcessingUnit
{
	private final ProcessingTools tools;
	public final TypeElement enclosingClass;
	private final List<PacketProcessingUnit> packets = new ArrayList<>();
	public final ClassName enclosingClassName;
	public final ClassName packetGroupClassName;
	private ProcessingStatus status = ProcessingStatus.SUCESSS;

	public PacketProcessingContext(ProcessingTools tools, TypeElement enclosingClass)
	{
	    this.tools = tools;
	    this.enclosingClass = enclosingClass;
	    this.enclosingClassName = tools.naming.buildClassName(enclosingClass);
		this.packetGroupClassName = getPacketGroupName(this.enclosingClassName, "");
	}

	public static ClassName getPacketGroupName(ClassName enclosingClassName, String userDefinedName)
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

	public void addPacket(ExecutableElement method, PacketDestination dest, Optional<? extends AnnotationMirror> annotation)
	{
	    this.packets.add(new PacketProcessingUnit(this.tools, this, method, dest, annotation,
	    	this.tools.elements.getAnnotationMirror(method, NetworkAdaptable.class), method.getAnnotation(NetworkAdaptable.class),
	    	this.tools.elements.getAnnotationMirror(method, NetworkSerializable.class), method.getAnnotation(NetworkSerializable.class)));
	}

	@Override
	public ClassName getTargetClassName()
	{
		return this.enclosingClassName;
	}

	@Override
	public ProcessingStatus process()
	{
		List<String> lines = new ArrayList<>();
	    // Generate each packet class
	    for (PacketProcessingUnit packet : this.packets)
	    {
	        ProcessingStatus status = packet.process(lines::add);
	        if (status != ProcessingStatus.SUCESSS)
	        {
	        	return status;
	        }
	    }

		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.packetGroupClassName.packageName());
		replacements.put("className", this.packetGroupClassName.shortName());
		replacements.put("packets", lines.stream().map(line -> line + System.lineSeparator()).collect(Collectors.joining()));

		this.tools.templates.writeFileWithLog(this.packetGroupClassName.qualifiedName(), "templates/TemplatePacketGroup.jvtp", replacements, this.enclosingClass, Optional.empty(), this.enclosingClass);

	    return ProcessingStatus.SUCESSS;
	}

}
