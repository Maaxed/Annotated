package fr.max2.annotated.processor.util.model.type;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;


public class TestingArrayType extends TestingType implements ArrayType
{
	private final TypeMirror componentType;
	
	public TestingArrayType(TypeMirror componentType)
	{
		super(TypeKind.ARRAY);
		this.componentType = componentType;
	}
	
	@Override
	public TypeMirror getComponentType()
	{
		return this.componentType;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitArray(this, p);
	}
	
}
