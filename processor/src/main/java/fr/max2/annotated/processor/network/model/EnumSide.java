package fr.max2.annotated.processor.network.model;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.ServerPacket;

import java.lang.annotation.Annotation;

/**
 * Represents the logical sides a packet can be sent to
 */
public enum EnumSide
{
	CLIENT("Client", ClientPacket.class),
	SERVER("Server", ServerPacket.class);
	
	private final String name;
	private final Class<? extends Annotation> annotation;
	
	private EnumSide(String name, Class<? extends Annotation> annotation)
	{
		this.name = name;
		this.annotation = annotation;
	}
	
	public EnumSide opposite()
	{
		switch (this)
		{
		case CLIENT:
			return SERVER;
		case SERVER:
			return CLIENT;
		default:
			throw new IllegalStateException("Unknown side '" + this.toString() + "'");
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
