package fr.max2.annotated.processor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.CoreMatchers.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import static org.hamcrest.MatcherAssert.*;
import org.junit.Test;

import fr.max2.annotated.processor.network.model.IImportClassBuilder;


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
	
	@Test
	public void testProvideTypeImports()
	{
		this.setUpModel();
		
		Set<String> imports = new HashSet<>();
		IImportClassBuilder<?> importer = new Importer(imports);

		
		this.helper.provideTypeImports(this.primitive, importer);
		assertEquals(0, imports.size());
		imports.clear();
		
		this.helper.provideTypeImports(this.integer, importer);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("java.lang.Integer"));
		imports.clear();
		
		this.helper.provideTypeImports(this.list, importer);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems("java.util.List", "java.lang.Integer"));
		imports.clear();
		
		this.helper.provideTypeImports(this.map, importer);
		assertEquals(3, imports.size());
		assertThat(imports, hasItems("java.util.Map", "java.lang.Integer", "java.util.List"));
		imports.clear();
		
		this.helper.provideTypeImports(this.array, importer);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("java.lang.Integer"));
		imports.clear();
		
		this.helper.provideTypeImports(this.simpleTypeVariable, importer);
		assertEquals(0, imports.size());
		imports.clear();
		
		this.helper.provideTypeImports(this.simpleWildcard, importer);
		assertEquals(0, imports.size());
		imports.clear();
		
		this.helper.provideTypeImports(this.extendsWildcard, importer);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("java.lang.Integer"));
		imports.clear();
		
		this.helper.provideTypeImports(this.superWildcard, importer);
		assertEquals(1, imports.size());
		assertThat(imports, hasItem("fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		imports.clear();
		
		this.helper.provideTypeImports(this.union, importer);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems("java.lang.Integer", "fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		imports.clear();
		
		this.helper.provideTypeImports(this.intersaction, importer);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems("java.lang.Integer", "fr.max2.annotated.processor.utils.NamingUtils.TypeToString"));
		imports.clear();
	}
	
	private static class Importer implements IImportClassBuilder<Importer>
	{
		Collection<String> imports;
		
		private Importer(Collection<String> imports)
		{
			this.imports = imports;
		}
		
		@Override
		public Importer addImport(String className)
		{
			this.imports.add(className);
			return this;
		}
		
		@Override
		public Importer addImport(ClassName className)
		{
			this.imports.add(className.qualifiedName());
			return this;
		}
		
		@Override
		public Importer addImport(TypeElement classElem)
		{
			this.imports.add(classElem.getQualifiedName().toString());
			return this;
		}
	}
}
