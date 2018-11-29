package fr.max2.packeta.utils;

import java.util.Set;

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

	public void addImports(Set<String> imports)
	{
		if (this.isClient()) imports.add(ClassRef.CLIENT_MESSAGE);
		if (this.isServer()) imports.add(ClassRef.SERVER_MESSAGE);
	}
	
}
