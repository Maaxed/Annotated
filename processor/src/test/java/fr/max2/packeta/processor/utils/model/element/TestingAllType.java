package fr.max2.packeta.processor.utils.model.element;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import org.junit.Assert;


public class TestingAllType implements PrimitiveType, NullType, ArrayType, ErrorType, TypeVariable, WildcardType, ExecutableType, NoType, UnionType, IntersectionType
{

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
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeKind getKind()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public Element asElement()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getEnclosingType()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments()
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
	public List<? extends TypeMirror> getAlternatives()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public List<? extends TypeVariable> getTypeVariables()
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
	public List<? extends TypeMirror> getParameterTypes()
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
	public List<? extends TypeMirror> getThrownTypes()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getExtendsBound()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getSuperBound()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getUpperBound()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getLowerBound()
	{
		Assert.fail("Illegal method call");
		return null;
	}

	@Override
	public TypeMirror getComponentType()
	{
		Assert.fail("Illegal method call");
		return null;
	}
	
	
}
