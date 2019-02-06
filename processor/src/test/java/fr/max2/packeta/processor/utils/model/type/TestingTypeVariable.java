package fr.max2.packeta.processor.utils.model.type;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import org.junit.Assert;


public class TestingTypeVariable extends TestingType implements TypeVariable
{
	private final Element thisElement;
	private final TypeMirror upperBound, lowerBond;
	
	public TestingTypeVariable(Element thisElement, TypeMirror upperBound, TypeMirror lowerBond)
	{
		super(TypeKind.TYPEVAR);
		this.thisElement = thisElement;
		this.upperBound = upperBound;
		this.lowerBond = lowerBond;
	}
	
	@Override
	public Element asElement()
	{
		if (this.thisElement == null) Assert.fail("Illegal method call");
		return this.thisElement;
	}
	
	@Override
	public TypeMirror getUpperBound()
	{
		if (this.upperBound == null) Assert.fail("Illegal method call");
		return this.upperBound;
	}
	
	@Override
	public TypeMirror getLowerBound()
	{
		if (this.lowerBond == null) Assert.fail("Illegal method call");
		return this.lowerBond;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitTypeVariable(this, p);
	}
	
}
