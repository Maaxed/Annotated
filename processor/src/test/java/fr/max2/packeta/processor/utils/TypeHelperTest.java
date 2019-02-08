package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;
import static fr.max2.packeta.processor.utils.TypeHelper.*;

import org.junit.Test;


public class TypeHelperTest extends TestModelProvider
{
	
	@Test
	public void testAsTypeElement()
	{
		this.setUpModel();

		assertEquals(this.intElement, asTypeElement(this.intElement));
		assertNull(asTypeElement(this.packageType));
	}
	
	@Test
	public void testAsArrayType()
	{
		this.setUpModel();
		
		assertEquals(this.array, asArrayType(this.array));
		assertNull(asArrayType(this.integer));
		assertNull(asArrayType(this.primitive));
	}
	
	@Test
	public void testAsWildcardType()
	{
		this.setUpModel();
		
		assertEquals(this.simpleWildcard, asWildcardType(this.simpleWildcard));
		assertEquals(this.extendsWildcard, asWildcardType(this.extendsWildcard));
		assertEquals(this.superWildcard, asWildcardType(this.superWildcard));
		assertNull(asWildcardType(this.integer));
		assertNull(asWildcardType(this.primitive));
	}
	
	@Test
	public void testAsVariableType()
	{
		this.setUpModel();
		
		assertEquals(this.simpleTypeVariable, asVariableType(this.simpleTypeVariable));
		assertNull(asVariableType(this.integer));
		assertNull(asVariableType(this.primitive));
	}
	
	@Test
	public void testAsIntersectionType()
	{
		this.setUpModel();
		
		assertEquals(this.intersaction, asIntersectionType(this.intersaction));
		assertNull(asIntersectionType(this.integer));
		assertNull(asIntersectionType(this.primitive));
	}
	
	@Test
	public void testAsPackage()
	{
		this.setUpModel();

		assertEquals(this.packageType, asPackage(this.packageType));
		assertNull(asPackage(this.intElement));
	}
	
}
