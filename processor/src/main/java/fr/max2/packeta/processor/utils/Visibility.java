package fr.max2.packeta.processor.utils;

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
		return PACKAGE;
	}
	
}
