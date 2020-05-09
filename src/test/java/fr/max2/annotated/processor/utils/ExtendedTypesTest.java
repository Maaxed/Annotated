package fr.max2.annotated.processor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.CoreMatchers.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import org.junit.Test;


public class TypeHelperTest extends TestModelProvider
{
	private TypeHelper helper = new TypeHelper(null);
	
	@Test
	public void testAsTypeElement()
	{
		this.setUpModel();

		assertEquals(this.intElement, helper.asTypeElement(this.intElement));
		assertNull(helper.asTypeElement(this.packageType));
	}
	
	@Test
	public void testAsArrayType()
	{
		this.setUpModel();
		
		assertEquals(this.array, helper.asArrayType(this.array));
		assertNull(helper.asArrayType(this.integer));
		assertNull(helper.asArrayType(this.primitive));
	}
	
	@Test
	public void testAsWildcardType()
	{
		this.setUpModel();
		
		assertEquals(this.simpleWildcard, helper.asWildcardType(this.simpleWildcard));
		assertEquals(this.extendsWildcard, helper.asWildcardType(this.extendsWildcard));
		assertEquals(this.superWildcard, helper.asWildcardType(this.superWildcard));
		assertNull(helper.asWildcardType(this.integer));
		assertNull(helper.asWildcardType(this.primitive));
	}
	
	@Test
	public void testAsVariableType()
	{
		this.setUpModel();
		
		assertEquals(this.simpleTypeVariable, helper.asVariableType(this.simpleTypeVariable));
		assertNull(helper.asVariableType(this.integer));
		assertNull(helper.asVariableType(this.primitive));
	}
	
	@Test
	public void testAsIntersectionType()
	{
		this.setUpModel();
		
		assertEquals(this.intersaction, helper.asIntersectionType(this.intersaction));
		assertNull(helper.asIntersectionType(this.integer));
		assertNull(helper.asIntersectionType(this.primitive));
	}
	
	@Test
	public void testAsPackage()
	{
		this.setUpModel();

		assertEquals(this.packageType, helper.asPackage(this.packageType));
		assertNull(helper.asPackage(this.intElement));
	}
	
	@Test
	public void testProvideTypeImports()
	{
		this.setUpModel();
		
		List<String> imports = new ArrayList<>();
		
		helper.provideTypeImports(this.integer, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.list, imports::add);
		assertEquals(1, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.map, imports::add);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems("java.util.Map", "java.util.List"));
		
		imports.clear();
		
		helper.provideTypeImports(this.primitive, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.array, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.simpleTypeVariable, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.simpleWildcard, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.extendsWildcard, imports::add);
		assertEquals(0, imports.size());
		
		imports.clear();
		
		helper.provideTypeImports(this.superWildcard, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		
		imports.clear();
		
		helper.provideTypeImports(this.union, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		
		imports.clear();
		
		helper.provideTypeImports(this.intersaction, imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
	}
	
}
