package fr.max2.annotated.processor.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

public interface DefaultElementVisitor<R, P> extends ElementVisitor<R, P>
{
	@Override
	default R visit(Element e, P p)
	{
		return e.accept(this, p);
	}
	
	@Override
	default R visit(Element e)
	{
		return e.accept(this, null);
	}
	
	@Override
	default R visitPackage(PackageElement e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	@Override
	default R visitType(TypeElement e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	@Override
	default R visitVariable(VariableElement e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	@Override
	default R visitExecutable(ExecutableElement e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	@Override
	default R visitTypeParameter(TypeParameterElement e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	@Override
	default R visitUnknown(Element e, P p)
	{
		return this.visitDefault(e, p);
	}
	
	R visitDefault(Element e, P p);
}
