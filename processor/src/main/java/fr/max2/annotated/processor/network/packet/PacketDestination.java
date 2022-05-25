package fr.max2.annotated.processor.network.packet;

import fr.max2.annotated.api.network.Packet;

/**
 * Represents the logical sides a packet can be sent to
 */
public enum PacketDestination
{
	CLIENT("Client", Packet.Destination.CLIENT),
	SERVER("Server", Packet.Destination.SERVER);

	private final String name;
	private final Packet.Destination annotationValue;

	private PacketDestination(String name, Packet.Destination annotationValue)
	{
		this.name = name;
		this.annotationValue = annotationValue;
	}

	public PacketDestination opposite()
	{
		switch (this)
		{
		case CLIENT:
			return SERVER;
		case SERVER:
			return CLIENT;
		default:
			throw new IllegalStateException("Unknown packet destination '" + this.toString() + "'");
		}
	}

	public Packet.Destination getAnnotationValue()
	{
		return annotationValue;
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

	public static PacketDestination fromAnnotationValue(Packet.Destination annotationValue)
	{
		return switch (annotationValue)
		{
			case CLIENT -> CLIENT;
			case SERVER -> SERVER;
			default -> null;
		};
	}
}
