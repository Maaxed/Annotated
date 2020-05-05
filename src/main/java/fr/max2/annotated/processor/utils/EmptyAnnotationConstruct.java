package fr.max2.annotated.processor.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;

public enum EmptyAnnotationConstruct implements AnnotatedConstruct
{
	INSTANCE;

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
	
}
