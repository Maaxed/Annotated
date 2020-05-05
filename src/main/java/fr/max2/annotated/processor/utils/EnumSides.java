package fr.max2.annotated.processor.utils;

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
	
	public String getSimpleName()
	{
		switch (this)
		{
		case CLIENT:
			return "Client";
		case SERVER:
			return "Server";
		case BOTH:
			return "Common";
		default:
			throw new IllegalStateException("Unknown side '" + this.toString() + "'");
		}
	}
	
}
