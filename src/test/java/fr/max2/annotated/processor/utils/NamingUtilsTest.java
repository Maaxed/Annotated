package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.utils.NamingUtils.TypeToString.*;
import static org.junit.Assert.*;

import org.junit.Test;

import fr.max2.annotated.processor.utils.model.type.TestingNullType;

public class NamingUtilsTest extends TestModelProvider
{
	
	@Test
	public void testComputeSimplifiedName()
	{
		this.setUpModel();
		
		assertEquals("Integer", SIMPLIFIED.computeName(integer));
		assertEquals("List<>", SIMPLIFIED.computeName(list));
		assertEquals("Map<>", SIMPLIFIED.computeName(map));
		assertEquals("double", SIMPLIFIED.computeName(primitive));
		assertEquals("Integer[]", SIMPLIFIED.computeName(array));
		assertEquals("E", SIMPLIFIED.computeName(simpleTypeVariable));
		assertEquals("?", SIMPLIFIED.computeName(simpleWildcard));
		assertEquals("? extends Integer", SIMPLIFIED.computeName(extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", SIMPLIFIED.computeName(superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", SIMPLIFIED.computeName(union));
		assertEquals("Integer & NamingUtils.TypeToString", SIMPLIFIED.computeName(intersaction));
		assertEquals("Null", SIMPLIFIED.computeName(TestingNullType.INSTANCE));
	}
	
	@Test
	public void testComputeFullName()
	{
		this.setUpModel();
		
		assertEquals("Integer", FULL.computeName(integer));
		assertEquals("List<Integer>", FULL.computeName(list));
		assertEquals("Map<Integer, List<Integer>>", FULL.computeName(map));
		assertEquals("double", FULL.computeName(primitive));
		assertEquals("Integer[]", FULL.computeName(array));
		assertEquals("E", FULL.computeName(simpleTypeVariable));
		assertEquals("?", FULL.computeName(simpleWildcard));
		assertEquals("? extends Integer", FULL.computeName(extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", FULL.computeName(superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", FULL.computeName(union));
		assertEquals("Integer & NamingUtils.TypeToString", FULL.computeName(intersaction));
		assertEquals("Null", FULL.computeName(TestingNullType.INSTANCE));
	}
	
}
