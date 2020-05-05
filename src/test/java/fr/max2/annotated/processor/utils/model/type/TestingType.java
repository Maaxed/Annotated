package fr.max2.annotated.processor.utils.model.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.junit.Assert;

public class TestingType implements TypeMirror
{
	private final TypeKind kind;

	public TestingType(TypeKind kind)
	{
		this.kind = kind;
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
	public TypeKind getKind()
	{
		return this.kind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		Assert.fail("Illegal method call");
		return null;
	}
	
}
