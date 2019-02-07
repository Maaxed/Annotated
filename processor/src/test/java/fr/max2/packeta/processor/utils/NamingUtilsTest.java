package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static fr.max2.packeta.processor.utils.NamingUtils.*;

import org.junit.Test;

import fr.max2.packeta.processor.utils.model.element.TestingTypeElement;
import fr.max2.packeta.processor.utils.model.type.TestingArrayType;
import fr.max2.packeta.processor.utils.model.type.TestingDeclaredType;
import fr.max2.packeta.processor.utils.model.type.TestingIntersectionType;
import fr.max2.packeta.processor.utils.model.type.TestingNullType;
import fr.max2.packeta.processor.utils.model.type.TestingPrimitiveType;
import fr.max2.packeta.processor.utils.model.type.TestingUnionType;
import fr.max2.packeta.processor.utils.model.type.TestingWildcardType;


public class NamingUtilsTest
{
	private TypeMirror integer, list, map;
	private TypeMirror primitive, array;
	private TypeMirror simpleTypeVariable;
	private TypeMirror simpleWildcard, extendsWildcard, superWildcard;
	private TypeMirror union, intersaction;
	
	@Test
	public void testSimpleName()
	{
		assertEquals("int", simpleName("int"));
		assertEquals("boolean", simpleName("boolean"));
		
		assertEquals("Integer", simpleName("java.lang.Integer"));
		assertEquals("Boolean", simpleName("java.lang.Boolean"));

		assertEquals("Inner", simpleName("net.thing.Outer.Inner"));
	}
	
	@Test
	public void testComputeSimplifiedName()
	{
		this.setUpTypes();
		
		assertEquals("Integer", computeSimplifiedName(integer));
		assertEquals("List<>", computeSimplifiedName(list));
		assertEquals("Map<>", computeSimplifiedName(map));
		assertEquals("double", computeSimplifiedName(primitive));
		assertEquals("Integer[]", computeSimplifiedName(array));
		assertEquals("E", computeSimplifiedName(simpleTypeVariable));
		assertEquals("*", computeSimplifiedName(simpleWildcard));
		assertEquals("* extends Integer", computeSimplifiedName(extendsWildcard));
		assertEquals("* super TypeToString", computeSimplifiedName(superWildcard));
		assertEquals("Integer | TypeToString", computeSimplifiedName(union));
		assertEquals("Integer & TypeToString", computeSimplifiedName(intersaction));
		assertEquals("Null", computeSimplifiedName(TestingNullType.INSTANCE));
	}
	
	@Test
	public void testComputeFullName()
	{
		this.setUpTypes();
		
		assertEquals("Integer", computeFullName(integer));
		assertEquals("List<Integer>", computeFullName(list));
		assertEquals("Map<Integer, List<Integer>>", computeFullName(map));
		assertEquals("double", computeFullName(primitive));
		assertEquals("Integer[]", computeFullName(array));
		assertEquals("E", computeFullName(simpleTypeVariable));
		assertEquals("*", computeFullName(simpleWildcard));
		assertEquals("* extends Integer", computeFullName(extendsWildcard));
		assertEquals("* super TypeToString", computeFullName(superWildcard));
		assertEquals("Integer | TypeToString", computeFullName(union));
		assertEquals("Integer & TypeToString", computeFullName(intersaction));
		assertEquals("Null", computeFullName(TestingNullType.INSTANCE));
	}
	
	private void setUpTypes()
	{
		this.integer = new TestingDeclaredType(new TestingTypeElement(ElementKind.CLASS, "java.lang.Integer"));
		
		TestingTypeElement genericTypeElement = new TestingTypeElement(ElementKind.INTERFACE, "java.util.List").withNewTypeParameter("E");
		this.list = new TestingDeclaredType(genericTypeElement, integer);
		
		TestingTypeElement complexTypeElement = new TestingTypeElement(ElementKind.INTERFACE, "java.util.Map").withNewTypeParameter("K").withNewTypeParameter("V");
		this.map = new TestingDeclaredType(complexTypeElement, integer, list);

		this.primitive = new TestingPrimitiveType(TypeKind.DOUBLE);
		this.array = new TestingArrayType(integer);
		
		this.simpleTypeVariable = genericTypeElement.getTypeParameters().get(0).asType();
		
		TypeMirror enumType = new TestingDeclaredType(new TestingTypeElement(ElementKind.ENUM, "fr.max2.packeta.processor.utils.NamingUtils.TypeToString"));
		
		this.simpleWildcard = new TestingWildcardType(null, null);
		this.extendsWildcard = new TestingWildcardType(this.integer, null);
		this.superWildcard = new TestingWildcardType(null, enumType);
		
		this.union = new TestingUnionType(this.integer, enumType);
		
		this.intersaction = new TestingIntersectionType(this.integer, enumType);
	}
	
}
