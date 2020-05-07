package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.utils.NamingUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import fr.max2.annotated.processor.utils.model.type.TestingNullType;

public class NamingUtilsTest extends TestModelProvider
{
	
	@Test
	public void testComputeSimplifiedName()
	{
		this.setUpModel();
		
		assertEquals("Integer", computeSimplifiedName(integer));
		assertEquals("List<>", computeSimplifiedName(list));
		assertEquals("Map<>", computeSimplifiedName(map));
		assertEquals("double", computeSimplifiedName(primitive));
		assertEquals("Integer[]", computeSimplifiedName(array));
		assertEquals("E", computeSimplifiedName(simpleTypeVariable));
		assertEquals("?", computeSimplifiedName(simpleWildcard));
		assertEquals("? extends Integer", computeSimplifiedName(extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", computeSimplifiedName(superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", computeSimplifiedName(union));
		assertEquals("Integer & NamingUtils.TypeToString", computeSimplifiedName(intersaction));
		assertEquals("Null", computeSimplifiedName(TestingNullType.INSTANCE));
	}
	
	@Test
	public void testComputeFullName()
	{
		this.setUpModel();
		
		assertEquals("Integer", computeFullName(integer));
		assertEquals("List<Integer>", computeFullName(list));
		assertEquals("Map<Integer, List<Integer>>", computeFullName(map));
		assertEquals("double", computeFullName(primitive));
		assertEquals("Integer[]", computeFullName(array));
		assertEquals("E", computeFullName(simpleTypeVariable));
		assertEquals("?", computeFullName(simpleWildcard));
		assertEquals("? extends Integer", computeFullName(extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", computeFullName(superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", computeFullName(union));
		assertEquals("Integer & NamingUtils.TypeToString", computeFullName(intersaction));
		assertEquals("Null", computeFullName(TestingNullType.INSTANCE));
	}
	
}
