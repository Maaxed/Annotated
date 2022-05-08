package fr.max2.annotated.processor.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ExtendedTypesTest extends TestModelProvider
{
	private ExtendedTypes helper = new ExtendedTypes(null, null);

	@Test
	public void testAsArrayType()
	{
		this.setUpModel();

		assertEquals(this.array, this.helper.asArrayType(this.array));
		assertNull(this.helper.asArrayType(this.integer));
		assertNull(this.helper.asArrayType(this.primitive));
	}

	@Test
	public void testAsWildcardType()
	{
		this.setUpModel();

		assertEquals(this.simpleWildcard, this.helper.asWildcardType(this.simpleWildcard));
		assertEquals(this.extendsWildcard, this.helper.asWildcardType(this.extendsWildcard));
		assertEquals(this.superWildcard, this.helper.asWildcardType(this.superWildcard));
		assertNull(this.helper.asWildcardType(this.integer));
		assertNull(this.helper.asWildcardType(this.primitive));
	}

	@Test
	public void testAsVariableType()
	{
		this.setUpModel();

		assertEquals(this.simpleTypeVariable, this.helper.asVariableType(this.simpleTypeVariable));
		assertNull(this.helper.asVariableType(this.integer));
		assertNull(this.helper.asVariableType(this.primitive));
	}

	@Test
	public void testAsIntersectionType()
	{
		this.setUpModel();

		assertEquals(this.intersaction, this.helper.asIntersectionType(this.intersaction));
		assertNull(this.helper.asIntersectionType(this.integer));
		assertNull(this.helper.asIntersectionType(this.primitive));
	}
}
