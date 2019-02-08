package fr.max2.packeta.processor.utils.model.type;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;


public class TestingNoType extends TestingType implements NoType
{
	public static final TestingNoType NONE = new TestingNoType(TypeKind.NONE);
	public static final TestingNoType VOID = new TestingNoType(TypeKind.VOID);
	public static final TestingNoType PACKAGE = new TestingNoType(TypeKind.PACKAGE);
	
	private TestingNoType(TypeKind kind)
	{
		super(kind);
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitNoType(this, p);
	}
	
}
