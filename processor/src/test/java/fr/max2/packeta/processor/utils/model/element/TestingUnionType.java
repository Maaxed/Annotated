package fr.max2.packeta.processor.utils.model.element;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;


public class TestingUnionType extends TestingType implements UnionType
{
	private final List<TypeMirror> alternatives;
	
	public TestingUnionType(TypeMirror... alternatives)
	{
		super(TypeKind.UNION);
		this.alternatives = Arrays.asList(alternatives);
	}
	
	@Override
	public List<? extends TypeMirror> getAlternatives()
	{
		return this.alternatives;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitUnion(this, p);
	}
	
}
