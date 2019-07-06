package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.utils.ValueInitStatus.*;
import static org.junit.Assert.*;

import org.junit.Test;


public class ValueInitStatusTest
{
	
	@Test
	public void testIsDeclared()
	{
		assertFalse(UNDEFINED.isDeclared());
		assertTrue(DECLARED.isDeclared());
		assertTrue(INITIALISED.isDeclared());
	}
	
	@Test
	public void testIsInitialised()
	{
		assertFalse(UNDEFINED.isInitialised());
		assertFalse(DECLARED.isInitialised());
		assertTrue(INITIALISED.isInitialised());
	}
	
}
