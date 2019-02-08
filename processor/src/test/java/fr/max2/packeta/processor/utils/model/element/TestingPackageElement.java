package fr.max2.packeta.processor.utils.model.element;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.PackageElement;

import fr.max2.packeta.processor.utils.model.type.TestingNoType;


public class TestingPackageElement extends TestingQualifiedNameable implements PackageElement
{
	
	public TestingPackageElement(String qualifiedName)
	{
		super(ElementKind.PACKAGE, TestingNoType.PACKAGE, qualifiedName);
	}
	
	@Override
	public boolean isUnnamed()
	{
		return this.getSimpleName().length() == 0;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitPackage(this, p);
	}
	
}
