package fr.max2.packeta.processor.utils.model.type;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.junit.Assert;

public class TestingElement implements PackageElement, TypeElement, VariableElement, ExecutableElement, TypeParameterElement
{

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror asType()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public ElementKind getKind()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Name getSimpleName()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Element getEnclosingElement()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends Element> getEnclosedElements()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType)
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Element getGenericElement()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends TypeMirror> getBounds()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getReturnType()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends VariableElement> getParameters()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getReceiverType()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public boolean isVarArgs()
	{
		Assert.fail("Illegal method call");
		return false;
	}

	@Override
	public boolean isDefault()
	{
		Assert.fail("Illegal method call");
		return false;
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public AnnotationValue getDefaultValue()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Object getConstantValue()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public NestingKind getNestingKind()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getSuperclass()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends TypeMirror> getInterfaces()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends TypeParameterElement> getTypeParameters()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Name getQualifiedName()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public boolean isUnnamed()
	{
		Assert.fail("Illegal method call");
		return false;
	}
	
}
