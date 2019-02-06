package fr.max2.packeta.processor.utils.model.element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import org.junit.Assert;

import fr.max2.packeta.processor.utils.model.SimpleName;

public class TestingElement implements Element
{
	private final ElementKind kind;
	private final TypeMirror correspondingType;
	private final Name name;
	private final List<Element> enclosedElements = new ArrayList<>();
	private final Set<Modifier> modifiers = new HashSet<>();
	
	public TestingElement(ElementKind kind, TypeMirror correspondingType, String name)
	{
		this.kind = kind;
		this.correspondingType = correspondingType;
		this.name = new SimpleName(name);
	}

	public void addEnclosedElement(Element element)
	{
		this.enclosedElements.add(element);
	}
	
	public void addModifier(Modifier modifier)
	{
		this.modifiers.add(modifier);
	}
	
	@Override
	public TypeMirror asType()
	{
		return this.correspondingType;
	}

	@Override
	public ElementKind getKind()
	{
		return this.kind;
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		return this.modifiers;
	}

	@Override
	public Name getSimpleName()
	{
		return this.name;
	}

	@Override
	public Element getEnclosingElement()
	{
		return null;
	}

	@Override
	public List<? extends Element> getEnclosedElements()
	{
		return this.enclosedElements;
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors()
	{
		return new ArrayList<>();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType)
	{
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
	{
		return (A[])Array.newInstance(annotationType, 0);
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		Assert.fail("Illegal method call");
		return null;
	}
	
}
