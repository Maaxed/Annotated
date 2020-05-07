package fr.max2.annotated.processor.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.model.element.TestingPackageElement;
import fr.max2.annotated.processor.utils.model.element.TestingTypeElement;
import fr.max2.annotated.processor.utils.model.type.TestingArrayType;
import fr.max2.annotated.processor.utils.model.type.TestingDeclaredType;
import fr.max2.annotated.processor.utils.model.type.TestingIntersectionType;
import fr.max2.annotated.processor.utils.model.type.TestingPrimitiveType;
import fr.max2.annotated.processor.utils.model.type.TestingUnionType;
import fr.max2.annotated.processor.utils.model.type.TestingWildcardType;

public class TestModelProvider
{
	protected TypeMirror integer, list, map;
	protected TypeMirror primitive, array;
	protected TypeMirror simpleTypeVariable;
	protected TypeMirror simpleWildcard, extendsWildcard, superWildcard;
	protected TypeMirror union, intersaction;
	
	protected Element packageType;
	protected TestingTypeElement intElement;
	
	protected void setUpModel()
	{
		this.intElement = new TestingTypeElement(ElementKind.CLASS, "java.lang", "Integer");
		this.integer = new TestingDeclaredType(this.intElement);
		
		TestingTypeElement genericTypeElement = new TestingTypeElement(ElementKind.INTERFACE, "java.util", "List").withNewTypeParameter("E");
		this.list = new TestingDeclaredType(genericTypeElement, integer);
		
		TestingTypeElement complexTypeElement = new TestingTypeElement(ElementKind.INTERFACE, "java.util", "Map").withNewTypeParameter("K").withNewTypeParameter("V");
		this.map = new TestingDeclaredType(complexTypeElement, integer, list);

		this.primitive = new TestingPrimitiveType(TypeKind.DOUBLE);
		this.array = new TestingArrayType(integer);
		
		this.simpleTypeVariable = genericTypeElement.getTypeParameters().get(0).asType();
		
		TypeMirror enumType = new TestingDeclaredType(new TestingTypeElement(ElementKind.ENUM, "fr.max2.annotated.processor.utils", "NamingUtils.TypeToString"));
		
		this.simpleWildcard = new TestingWildcardType(null, null);
		this.extendsWildcard = new TestingWildcardType(this.integer, null);
		this.superWildcard = new TestingWildcardType(null, enumType);
		
		this.union = new TestingUnionType(this.integer, enumType);
		
		this.intersaction = new TestingIntersectionType(this.integer, enumType);
		
		this.packageType = new TestingPackageElement("fr.max2.annotated");
	}
	
}
