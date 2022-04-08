package fr.max2.annotated.processor.network;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import fr.max2.annotated.processor.network.model.PacketDirection;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ProcessingTools;

public class PacketProcessingContext
{
	private final ProcessingTools tools;
	public final TypeElement enclosingClass;
	private final List<PacketProcessingUnit> packets = new ArrayList<>();
	public final ClassName enclosingClassName;
	private boolean hasErrors = false;
	
	public PacketProcessingContext(ProcessingTools tools, TypeElement enclosingClass)
	{
		this.tools = tools;
		this.enclosingClass = enclosingClass;
		this.enclosingClassName = tools.naming.buildClassName(enclosingClass);
	}
	
	public boolean hasErrors()
	{
		return this.hasErrors;
	}
	
	public void addPacket(ExecutableElement method, PacketDirection side)
	{
		this.packets.add(new PacketProcessingUnit(this.tools, this, method, side));
	}
	
	public void processPackets()
	{
		// Generate each packet class
		for (PacketProcessingUnit packet : this.packets)
		{
			packet.processPacket();
		}
		
		this.hasErrors = true;
	}
	
}
