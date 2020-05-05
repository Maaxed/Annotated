package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.utils.TypeHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.CoreMatchers.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
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
	
	@Test
	public void testProvideTypeImports()
	{
		this.setUpModel();
		
		List<String> imports = new ArrayList<>();
		
		provideTypeImports(this.integer, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.list, imports::add);
		assertEquals(1, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.map, imports::add);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems("java.util.Map", "java.util.List"));
		
		imports.clear();
		
		provideTypeImports(this.primitive, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.array, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.simpleTypeVariable, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.simpleWildcard, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.extendsWildcard, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		provideTypeImports(this.superWildcard, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		
		imports.clear();
		
		provideTypeImports(this.union, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		
		imports.clear();
		
		provideTypeImports(this.intersaction, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
	}
	
}
