package fr.max2.annotated.processor.utils;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class ExtendedElements implements Elements
{
	private final ProcessingTools tools;
	private final Elements base;
	
	ExtendedElements(ProcessingTools tools, Elements base)
	{
		this.tools = tools;
		this.base = base;
	}
	
	public TypeElement asTypeElement(Element elem)
	{
		return elem == null ? null : TypeElementCaster.INSTANCE.visit(elem);
	}
	
	private enum TypeElementCaster implements DefaultElementVisitor<TypeElement, Void>
	{
		INSTANCE;
		
		@Override
		public TypeElement visitType(TypeElement e, Void p)
		{
			return e;
		}
		
		@Override
		public TypeElement visitDefault(Element e, Void p)
		{
			return null;
		}
		
	}
	
	public PackageElement asPackage(Element type)
	{
		return type == null ? null : PackageElementCaster.INSTANCE.visit(type);
	}
	
	private enum PackageElementCaster implements DefaultElementVisitor<PackageElement, Void>
	{
		INSTANCE;
		
		@Override
		public PackageElement visitPackage(PackageElement e, Void p)
		{
			return e;
		}

		@Override
		public PackageElement visitDefault(Element e, Void p)
		{
			return null;
		}
		
	}
	
	public Optional<? extends AnnotationMirror> getAnnotationMirror(Element elem, CharSequence annotationType)
	{
		return elem.getAnnotationMirrors().stream().filter(a -> asTypeElement(a.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationType)).findAny();
	}
	public Optional<? extends AnnotationValue> getAnnotationValue(Optional<? extends AnnotationMirror> annotation, CharSequence propertyName)
	{
		return annotation
			.flatMap(an ->
				an.getElementValues().entrySet().stream()
				.filter(entry -> entry.getKey().getSimpleName().contentEquals(propertyName))
				.findAny()
				).map(entry -> entry.getValue());
	}

	public Optional<? extends AnnotationValue> getAnnotationValue(Element elem, CharSequence annotationType, CharSequence propertyName)
	{
		return getAnnotationValue(getAnnotationMirror(elem, annotationType), propertyName);
	}

	
	// Delegate Types methods

	@Override
	public PackageElement getPackageElement(CharSequence name)
	{
		return this.base.getPackageElement(name);
	}

	@Override
	public TypeElement getTypeElement(CharSequence name)
	{
		return this.base.getTypeElement(name);
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a)
	{
		return this.base.getElementValuesWithDefaults(a);
	}

	@Override
	public String getDocComment(Element e)
	{
		return this.base.getDocComment(e);
	}

	@Override
	public boolean isDeprecated(Element e)
	{
		return this.base.isDeprecated(e);
	}

	@Override
	public Name getBinaryName(TypeElement type)
	{
		return this.base.getBinaryName(type);
	}

	@Override
	public PackageElement getPackageOf(Element type)
	{
		return this.base.getPackageOf(type);
	}

	@Override
	public List<? extends Element> getAllMembers(TypeElement type)
	{
		return this.base.getAllMembers(type);
	}

	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e)
	{
		return this.base.getAllAnnotationMirrors(e);
	}

	@Override
	public boolean hides(Element hider, Element hidden)
	{
		return this.base.hides(hider, hidden);
	}

	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type)
	{
		return this.base.overrides(overrider, overridden, type);
	}

	@Override
	public String getConstantExpression(Object value)
	{
		return this.base.getConstantExpression(value);
	}

	@Override
	public void printElements(Writer w, Element... elements)
	{
		this.base.printElements(w, elements);
	}

	@Override
	public Name getName(CharSequence cs)
	{
		return this.base.getName(cs);
	}

	@Override
	public boolean isFunctionalInterface(TypeElement type)
	{
		return this.base.isFunctionalInterface(type);
	}
}
