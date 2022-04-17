package fr.max2.annotated.processor.util;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.max2.annotated.processor.util.model.type.TestingNullType;

public class NamingUtilsTest extends TestModelProvider
{
	
	@Test
	public void testComputeSimplifiedName()
	{
		this.setUpModel();
		
		/*assertEquals("Integer", SIMPLIFIED.computeName(this.integer));
		assertEquals("List<>", SIMPLIFIED.computeName(this.list));
		assertEquals("Map<>", SIMPLIFIED.computeName(this.map));
		assertEquals("double", SIMPLIFIED.computeName(this.primitive));
		assertEquals("Integer[]", SIMPLIFIED.computeName(this.array));
		assertEquals("E", SIMPLIFIED.computeName(this.simpleTypeVariable));
		assertEquals("?", SIMPLIFIED.computeName(this.simpleWildcard));
		assertEquals("? extends Integer", SIMPLIFIED.computeName(this.extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", SIMPLIFIED.computeName(this.superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", SIMPLIFIED.computeName(this.union));
		assertEquals("Integer & NamingUtils.TypeToString", SIMPLIFIED.computeName(this.intersaction));
		assertEquals("Null", SIMPLIFIED.computeName(TestingNullType.INSTANCE));*/
	}
	
	@Test
	public void testComputeFullName()
	{
		this.setUpModel();
		
		/*assertEquals("Integer", FULL.computeName(this.integer));
		assertEquals("List<Integer>", FULL.computeName(this.list));
		assertEquals("Map<Integer, List<Integer>>", FULL.computeName(this.map));
		assertEquals("double", FULL.computeName(this.primitive));
		assertEquals("Integer[]", FULL.computeName(this.array));
		assertEquals("E", FULL.computeName(this.simpleTypeVariable));
		assertEquals("?", FULL.computeName(this.simpleWildcard));
		assertEquals("? extends Integer", FULL.computeName(this.extendsWildcard));
		assertEquals("? super NamingUtils.TypeToString", FULL.computeName(this.superWildcard));
		assertEquals("Integer | NamingUtils.TypeToString", FULL.computeName(this.union));
		assertEquals("Integer & NamingUtils.TypeToString", FULL.computeName(this.intersaction));
		assertEquals("Null", FULL.computeName(TestingNullType.INSTANCE));*/
	}
	
}
