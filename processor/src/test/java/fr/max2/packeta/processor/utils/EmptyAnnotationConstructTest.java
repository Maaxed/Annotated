package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.junit.Test;

import fr.max2.packeta.api.processor.network.ConstSize;
import fr.max2.packeta.api.processor.network.CustomData;


public class EmptyAnnotationConstructTest
{
	private static final Class<?>[] TEST_ANNOTATIONS = {CustomData.class, ConstSize.class, Override.class};
	
	@Test
	public void testGetAnnotationMirrors()
	{
		assertTrue(EmptyAnnotationConstruct.INSTANCE.getAnnotationMirrors().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetAnnotation()
	{
		Stream.of(TEST_ANNOTATIONS).forEach(an -> testGetAnnotationFromClass((Class<? extends Annotation>) an));
	}

	void testGetAnnotationFromClass(Class<? extends Annotation> annotationClass)
	{
		assertNull(EmptyAnnotationConstruct.INSTANCE.getAnnotation(annotationClass));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetAnnotationsByType()
	{
		Stream.of(TEST_ANNOTATIONS).forEach(an -> testGetAnnotationsByTypeFromClass((Class<? extends Annotation>) an));
	}

	public void testGetAnnotationsByTypeFromClass(Class<? extends Annotation> annotationClass)
	{
		assertArrayEquals(new Object[0], EmptyAnnotationConstruct.INSTANCE.getAnnotationsByType(annotationClass));
	}
	
}
