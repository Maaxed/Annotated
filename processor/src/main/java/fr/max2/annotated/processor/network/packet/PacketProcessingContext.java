package fr.max2.annotated.processor.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
	private ProcessingStatus status = ProcessingStatus.SUCESSS;

	public PacketProcessingContext(ProcessingTools tools, TypeElement enclosingClass)
	{
	    this.tools = tools;
	    this.enclosingClass = enclosingClass;
	    this.enclosingClassName = tools.naming.buildClassName(enclosingClass);
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
	    // Generate each packet class
	    for (PacketProcessingUnit packet : this.packets)
	    {
	        packet.process();
	        if (packet.getStatus() != ProcessingStatus.SUCESSS)
	        {
	        	return packet.getStatus();
	        }
	    }

	    return ProcessingStatus.SUCESSS;
	}

}
