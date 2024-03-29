package fr.max2.annotated.processor.util;

import static fr.max2.annotated.processor.network.packet.PacketDestination.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.max2.annotated.api.network.Packet;

public class PacketDestinationTest
{
	
	@Test
	public void testOpposite()
	{
		assertEquals(SERVER, CLIENT.opposite());
		assertEquals(CLIENT, SERVER.opposite());
	}
	
	@Test
	public void testAnnotationClass()
	{
		assertEquals(Packet.Destination.CLIENT, CLIENT.getAnnotationValue());
		assertEquals(Packet.Destination.SERVER, SERVER.getAnnotationValue());
	}
	
	@Test
	public void testIsClient()
	{
		assertTrue(CLIENT.isClient());
		assertFalse(SERVER.isClient());
	}
	
	@Test
	public void testIsServer()
	{
		assertFalse(CLIENT.isServer());
		assertTrue(SERVER.isServer());
	}
	
	@Test
	public void testGetSimpleName()
	{
		assertEquals("Client", CLIENT.getSimpleName());
		assertEquals("Server", SERVER.getSimpleName());
	}
	
}
