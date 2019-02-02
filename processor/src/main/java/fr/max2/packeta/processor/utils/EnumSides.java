package fr.max2.packeta.processor.utils;

import java.util.function.Consumer;

/**
 * The Enum represent the logical sides a packet can be send to
 */
public enum EnumSides
{
	CLIENT,
	SERVER,
	BOTH;
	
	public boolean isClient()
	{
		return this == CLIENT || this == BOTH;
	}
	
	public boolean isServer()
	{
		return this == SERVER || this == BOTH;
	}
	
	public String getInterfaces()
	{
		switch (this)
		{
		case CLIENT:
			return ClassRef.CLIENT_MESSAGE_NAME;
		case SERVER:
			return ClassRef.SERVER_MESSAGE_NAME;
		case BOTH:
			return ClassRef.CLIENT_MESSAGE_NAME + ", " + ClassRef.SERVER_MESSAGE_NAME;
		default:
			throw new IllegalStateException("Unknown side '" + this.toString() + "'");
		}
	}
	
	public String getSimpleName()
	{
		switch (this)
		{
		case CLIENT:
			return "Client";
		case SERVER:
			return "Server";
		case BOTH:
			return "BothSides";
		default:
			throw new IllegalStateException("Unknown side '" + this.toString() + "'");
		}
	}

	public void addImports(Consumer<String> imports)
	{
		if (this.isClient()) imports.accept(ClassRef.CLIENT_MESSAGE);
		if (this.isServer()) imports.accept(ClassRef.SERVER_MESSAGE);
	}
	
}
