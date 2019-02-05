package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;
import static fr.max2.packeta.processor.utils.NamingUtils.*;

import org.junit.Test;


public class NamingUtilsTest
{
	
	@Test
	public void testSimpleName()
	{
		assertEquals("int", simpleName("int"));
		assertEquals("boolean", simpleName("boolean"));
		
		assertEquals("Integer", simpleName("java.lang.Integer"));
		assertEquals("Boolean", simpleName("java.lang.Boolean"));

		assertEquals("Inner", simpleName("net.thing.Outer.Inner"));
	}
	
	@Test
	public void testSimplifiedTypeName()
	{
		fail("Not yet implemented");
	}
	
	@Test
	public void testSimpleTypeName()
	{
		fail("Not yet implemented");
	}
	
}
