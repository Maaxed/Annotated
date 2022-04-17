package fr.max2.annotated.processor.util.model.type;

import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import fr.max2.annotated.processor.util.model.element.TestingTypeElement;


public class TestingErrorType extends TestingDeclaredType implements ErrorType
{
	
	public TestingErrorType(TestingTypeElement thisElement, TypeMirror... typeArgs)
	{
		super(TypeKind.ERROR, thisElement, typeArgs);
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitError(this, p);
	}
	
}
