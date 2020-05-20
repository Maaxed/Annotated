package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.network.model.EnumSide.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.ServerPacket;

public class EnumSideTest
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
		assertEquals(ClientPacket.class, CLIENT.getAnnotationClass());
		assertEquals(ServerPacket.class, SERVER.getAnnotationClass());
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
