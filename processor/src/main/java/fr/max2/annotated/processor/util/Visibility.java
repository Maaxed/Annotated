package fr.max2.annotated.processor.util;

import java.util.Set;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public enum Visibility
{
	PUBLIC,
	PROTECTED,
	PACKAGE,
	PRIVATE;
	
	public final Predicate<Element> filterAtLeast = elem -> Visibility.getElementVisibility(elem).isAtLeast(this);
	public final Predicate<Element> filterAtMost = elem -> Visibility.getElementVisibility(elem).isAtMost(this);
	
	public boolean isAtLeast(Visibility other)
	{
		return this.ordinal() <= other.ordinal();
	}
	
	public boolean isAtMost(Visibility other)
	{
		return this.ordinal() >= other.ordinal();
	}
	
	public static Visibility getElementVisibility(Element elem)
	{
		Set<Modifier> modifiers = elem.getModifiers();
		
		if (modifiers.contains(Modifier.PUBLIC)) return PUBLIC;
		if (modifiers.contains(Modifier.PROTECTED)) return Visibility.PROTECTED;
		if (modifiers.contains(Modifier.PRIVATE)) return PRIVATE;
		Element enclosing = elem.getEnclosingElement();
		return enclosing == null || enclosing.getKind().isInterface() ? PUBLIC : PACKAGE;
	}

	public static Visibility getTopLevelVisibility(Element elem)
	{
		Visibility v = PUBLIC;
		while (elem != null && (elem.getKind().isClass() || elem.getKind().isInterface()))
		{
			Visibility newV = getElementVisibility(elem);
			if (newV.isAtMost(v))
			{
				v = newV;
				if (v == Visibility.PRIVATE)
					return Visibility.PRIVATE;
			}

			elem = elem.getEnclosingElement();
		}
		return v;
	}
	
}
