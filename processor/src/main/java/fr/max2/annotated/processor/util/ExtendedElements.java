package fr.max2.annotated.processor.util;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.util.Elements;

public class ExtendedElements implements Elements
{
	private final Elements base;
	
	ExtendedElements(ProcessingTools tools, Elements base)
	{
		this.base = base;
	}
	
	public TypeElement asTypeElement(Element elem)
	{
		if (elem == null)
			return null;
		
		return this.typeElementCaster.visit(elem);
	}
	
	private final DefaultElementVisitor<TypeElement, Void> typeElementCaster = new DefaultElementVisitor<>()
	{
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
	};
	
	public PackageElement asPackage(Element type)
	{
		if (type == null)
			return null;
		
		return this.packageElementCaster.visit(type);
	}
	
	private final DefaultElementVisitor<PackageElement, Void> packageElementCaster = new DefaultElementVisitor<>()
	{
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
	};
	
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
	public PackageElement getPackageElement(ModuleElement module, CharSequence name)
	{
		return this.base.getPackageElement(module, name);
	}
	
	@Override
	public Set<? extends PackageElement> getAllPackageElements(CharSequence name)
	{
		return this.base.getAllPackageElements(name);
	}

	@Override
	public TypeElement getTypeElement(CharSequence name)
	{
		return this.base.getTypeElement(name);
	}
	
	@Override
	public TypeElement getTypeElement(ModuleElement module, CharSequence name)
	{
		return this.base.getTypeElement(module, name);
	}
	
	@Override
	public Set<? extends TypeElement> getAllTypeElements(CharSequence name)
	{
		return this.base.getAllTypeElements(name);
	}
	
	@Override
	public ModuleElement getModuleElement(CharSequence name)
	{
		return this.base.getModuleElement(name);
	}
	
	@Override
	public Set<? extends ModuleElement> getAllModuleElements()
	{
		return this.base.getAllModuleElements();
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
	public Origin getOrigin(Element e)
	{
		return this.base.getOrigin(e);
	}
	
	@Override
	public Origin getOrigin(AnnotatedConstruct c, AnnotationMirror a)
	{
		return this.base.getOrigin(c, a);
	}
	
	@Override
	public Origin getOrigin(ModuleElement m, Directive directive)
	{
		return this.base.getOrigin(m, directive);
	}
	
	@Override
	public boolean isBridge(ExecutableElement e)
	{
		return this.base.isBridge(e);
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
	public ModuleElement getModuleOf(Element e)
	{
		return this.base.getModuleOf(e);
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
	
	@Override
	public boolean isAutomaticModule(ModuleElement module)
	{
		return this.base.isAutomaticModule(module);
	}
	@Override
	public RecordComponentElement recordComponentFor(ExecutableElement accessor)
	{
		return this.base.recordComponentFor(accessor);
	}
	
	
}
