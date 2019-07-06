package fr.max2.annotated.processor.utils.model.type;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;

public class TestingWildcardType extends TestingType implements WildcardType
{
	private final TypeMirror extendsBound, superBound;
	
	public TestingWildcardType(TypeMirror extendsBound, TypeMirror superBound)
	{
		super(TypeKind.WILDCARD);
		this.extendsBound = extendsBound;
		this.superBound = superBound;
	}
	
	@Override
	public TypeMirror getExtendsBound()
	{
		return this.extendsBound;
	}
	
	@Override
	public TypeMirror getSuperBound()
	{
		return this.superBound;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitWildcard(this, p);
	}
	
}
