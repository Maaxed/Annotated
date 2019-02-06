package fr.max2.packeta.processor.utils.model.type;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;


public class TestingNullType extends TestingType implements NullType
{
	public static final TestingNullType INSTANCE = new TestingNullType();
	
	private TestingNullType()
	{
		super(TypeKind.NULL);
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitNull(this, p);
	}
	
}
