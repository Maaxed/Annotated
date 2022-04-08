package fr.max2.annotated.processor.network.model;

import java.lang.annotation.Annotation;

import fr.max2.annotated.api.network.ClientPacket;
import fr.max2.annotated.api.network.ServerPacket;

/**
 * Represents the logical sides a packet can be sent to
 */
public enum PacketDirection
{
	// TODO [v3.0] Rename enum values
	CLIENT("Client", ClientPacket.class),
	SERVER("Server", ServerPacket.class);
	
	private final String name;
	private final Class<? extends Annotation> annotation;
	
	private PacketDirection(String name, Class<? extends Annotation> annotation)
	{
		this.name = name;
		this.annotation = annotation;
	}
	
	public PacketDirection opposite()
	{
		switch (this)
		{
		case CLIENT:
			return SERVER;
		case SERVER:
			return CLIENT;
		default:
			throw new IllegalStateException("Unknown logical side '" + this.toString() + "'");
		}
	}
	
	public Class<? extends Annotation> getAnnotationClass()
	{
		return this.annotation;
	}
	
	public boolean isClient()
	{
		return this == CLIENT;
	}

	public boolean isServer()
	{
		return this == SERVER;
	}
	
	public String getSimpleName()
	{
		return this.name;
	}
	
}
