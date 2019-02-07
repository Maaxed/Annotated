package fr.max2.packeta.processor.utils.model.element;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.processor.utils.model.type.TestingDeclaredType;
import fr.max2.packeta.processor.utils.model.type.TestingIntersectionType;
import fr.max2.packeta.processor.utils.model.type.TestingNullType;
import fr.max2.packeta.processor.utils.model.type.TestingTypeVariable;


public class TestingTypeParameterElement extends TestingElement implements TypeParameterElement
{
	private final Element genericElement;
	private final List<TypeMirror> bounds;

	public TestingTypeParameterElement(String name, TypeElement execElement, TypeMirror... bounds)
	{
		super(ElementKind.PARAMETER, null, name);
		// The bounds list is shared between the element and the type.
		this.bounds = Arrays.asList(bounds);
		this.correspondingType = new TestingTypeVariable(this, this.bounds.isEmpty() ? TestingDeclaredType.OBJECT : new TestingIntersectionType(this.bounds), TestingNullType.INSTANCE);
		this.genericElement = execElement;
	}
	
	public TestingTypeParameterElement(String name, TestingExecutableElement execElement, TypeMirror... bounds)
	{
		this(name, (TypeElement)execElement, bounds);
		execElement.addTypeParameter(this);
	}
	
	@Override
	public Element getGenericElement()
	{
		return this.genericElement;
	}
	
	@Override
	public Element getEnclosingElement()
	{
		return this.genericElement;
	}
	
	@Override
	public List<? extends TypeMirror> getBounds()
	{
		return this.bounds;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitTypeParameter(this, p);
	}
	
}
