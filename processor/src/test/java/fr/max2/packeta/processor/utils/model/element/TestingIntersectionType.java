package fr.max2.packeta.processor.utils.model.element;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;


public class TestingIntersectionType extends TestingType implements IntersectionType
{
	private final List<TypeMirror> bounds;
	
	public TestingIntersectionType(TypeMirror... bounds)
	{
		super(TypeKind.INTERSECTION);
		this.bounds = Arrays.asList(bounds);
	}
	
	@Override
	public List<? extends TypeMirror> getBounds()
	{
		return this.bounds;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitIntersection(this, p);
	}
	
}
