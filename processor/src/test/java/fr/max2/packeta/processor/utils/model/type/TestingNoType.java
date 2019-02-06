package fr.max2.packeta.processor.utils.model.type;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;


public class TestingNoType extends TestingType implements NoType
{
	public static final TestingNoType INSTANCE = new TestingNoType();
	
	private TestingNoType()
	{
		super(TypeKind.NONE);
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitNoType(this, p);
	}
	
}
