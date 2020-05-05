package fr.max2.annotated.processor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static fr.max2.annotated.processor.utils.EnumSides.*;

import org.junit.Test;

public class EnumSidesTest
{
	
	@Test
	public void testIsClient()
	{
		assertTrue(CLIENT.isClient());
		assertTrue(BOTH.isClient());
		
		assertFalse(SERVER.isClient());
	}
	
	@Test
	public void testIsServer()
	{
		assertFalse(CLIENT.isServer());
		
		assertTrue(BOTH.isServer());
		assertTrue(SERVER.isServer());
	}
	
	@Test
	public void testGetSimpleName()
	{
		assertEquals("Client", CLIENT.getSimpleName());
		assertEquals("Server", SERVER.getSimpleName());
		assertEquals("Common", BOTH.getSimpleName());
	}
	
}
