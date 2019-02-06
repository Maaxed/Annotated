package fr.max2.packeta.processor.utils.model.element;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;


public class TestingTypeParameterElement extends TestingElement implements TypeParameterElement
{
	private final Element genericElement;
	private final List<TypeMirror> bounds = new ArrayList<>();
	
	public TestingTypeParameterElement(ElementKind kind, TypeMirror correspondingType, String name, TestingTypeElement typeElement)
	{
		super(kind, correspondingType, name);
		this.genericElement = typeElement;
		typeElement.addTypeParameter(this);
	}
	
	public TestingTypeParameterElement(ElementKind kind, TypeMirror correspondingType, String name, TestingExecutableElement execElement)
	{
		super(kind, correspondingType, name);
		this.genericElement = execElement;
		execElement.addTypeParameter(this);
	}
	
	public void addGenericType(TypeMirror bound)
	{
		this.bounds.add(bound);
	}
	
	@Override
	public Element getGenericElement()
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
