package fr.max2.annotated.processor.util;

import static fr.max2.annotated.processor.util.Visibility.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import org.junit.Test;

import fr.max2.annotated.processor.util.model.element.TestingAllElement;


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
		assertElementVisibity(PUBLIC, false, Modifier.PUBLIC);
		assertElementVisibity(PROTECTED, false, Modifier.PROTECTED);
		assertElementVisibity(PACKAGE, false);
		assertElementVisibity(PRIVATE, false, Modifier.PRIVATE);

		assertElementVisibity(PUBLIC, false, Modifier.PUBLIC, Modifier.ABSTRACT);
		assertElementVisibity(PROTECTED, false, Modifier.PROTECTED, Modifier.STATIC);
		assertElementVisibity(PACKAGE, false, Modifier.FINAL);
		assertElementVisibity(PRIVATE, false, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL, Modifier.ABSTRACT);
	}

	private static void assertElementVisibity(Visibility expected, boolean isParentInterface, Modifier... modifiers)
	{
		assertEquals(expected, getElementVisibility(new VisibilityTestingElement(new VisibilityParentTestingElement(isParentInterface), modifiers)));
	}

	private static class VisibilityTestingElement extends TestingAllElement
	{
		private final Element enclosingElem;
		private final Set<Modifier> modifiers;

		public VisibilityTestingElement(Element enclosingElem, Modifier... modifiers)
		{
			this.enclosingElem = enclosingElem;
			this.modifiers = new HashSet<>(Arrays.asList(modifiers));
		}

		@Override
		public Set<Modifier> getModifiers()
		{
			return this.modifiers;
		}

		@Override
		public Element getEnclosingElement()
		{
			return this.enclosingElem;
		}
	}

	private static class VisibilityParentTestingElement extends TestingAllElement
	{
		private final boolean isInterface;

		public VisibilityParentTestingElement(boolean isInterface)
		{
			this.isInterface = isInterface;
		}

		@Override
		public ElementKind getKind()
		{
			return this.isInterface ? ElementKind.INTERFACE : ElementKind.CLASS;
		}
	}

}
