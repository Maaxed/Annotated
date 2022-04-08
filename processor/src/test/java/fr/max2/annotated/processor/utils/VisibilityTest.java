package fr.max2.annotated.processor.utils;

import static fr.max2.annotated.processor.utils.Visibility.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.junit.Test;

import fr.max2.annotated.processor.utils.model.element.TestingAllElement;


public class VisibilityTest
{
	
	@Test
	public void testIsAtLeast()
	{
		assertAtLeast(PUBLIC, PUBLIC, PROTECTED, PACKAGE, PRIVATE);
		assertAtLeast(PROTECTED, PROTECTED, PACKAGE, PRIVATE);
		assertAtLeast(PACKAGE, PACKAGE, PRIVATE);
		assertAtLeast(PRIVATE, PRIVATE);
	}
	
	private static void assertAtLeast(Visibility tested, Visibility... expected)
	{
		List<Visibility> found = new ArrayList<>();
		for (Visibility v : values())
		{
			if (tested.isAtLeast(v))
			{
				found.add(v);
			}
		}
		assertArrayEquals(expected, found.toArray());
	}
	
	@Test
	public void testIsAtMost()
	{
		assertAtMost(PUBLIC, PUBLIC);
		assertAtMost(PROTECTED, PUBLIC, PROTECTED);
		assertAtMost(PACKAGE, PUBLIC, PROTECTED, PACKAGE);
		assertAtMost(PRIVATE, PUBLIC, PROTECTED, PACKAGE, PRIVATE);
	}
	
	private static void assertAtMost(Visibility tested, Visibility... expected)
	{
		List<Visibility> found = new ArrayList<>();
		for (Visibility v : values())
		{
			if (tested.isAtMost(v))
			{
				found.add(v);
			}
		}
		assertArrayEquals(expected, found.toArray());
	}
	
	@Test
	public void testGetElementVisibility()
	{
		assertElementVisibity(PUBLIC, Modifier.PUBLIC);
		assertElementVisibity(PROTECTED, Modifier.PROTECTED);
		assertElementVisibity(PACKAGE);
		assertElementVisibity(PRIVATE, Modifier.PRIVATE);

		assertElementVisibity(PUBLIC, Modifier.PUBLIC, Modifier.ABSTRACT);
		assertElementVisibity(PROTECTED, Modifier.PROTECTED, Modifier.STATIC);
		assertElementVisibity(PACKAGE, Modifier.FINAL);
		assertElementVisibity(PRIVATE, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL, Modifier.ABSTRACT);
	}
	
	private static void assertElementVisibity(Visibility expected, Modifier... modifiers)
	{
		assertEquals(expected, getElementVisibility(new VisibilityTestingElement(modifiers)));
	}
	
	private static class VisibilityTestingElement extends TestingAllElement
	{
		private final Set<Modifier> modifiers;

		public VisibilityTestingElement(Modifier... modifiers)
		{
			this.modifiers = new HashSet<>(Arrays.asList(modifiers));
		}
		
		@Override
		public Set<Modifier> getModifiers()
		{
			return this.modifiers;
		}
	}
	
}
