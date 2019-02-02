package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;

import org.junit.Test;


public class HolderTest
{
	
	@Test
	public void testHolder()
	{
		Holder<String> empty = new Holder<>();
		assertNull(empty.getValue());
		
		Holder<String> h1 = new Holder<>("Test");
		assertEquals("Test", h1.getValue());
		
		h1.setValue("Test2");
		assertEquals("Test2", h1.getValue());
	}
	
}
