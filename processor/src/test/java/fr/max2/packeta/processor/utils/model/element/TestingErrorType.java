package fr.max2.packeta.processor.utils.model.element;

import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;


public class TestingErrorType extends TestingDeclaredType implements ErrorType
{
	
	public TestingErrorType(Element thisElement, TypeMirror... typeArgs)
	{
		super(thisElement, typeArgs);
	}
	
	public TestingErrorType(TypeMirror... typeArgs)
	{
		super(typeArgs);
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitError(this, p);
	}
	
}
