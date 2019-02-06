package fr.max2.packeta.processor.utils.model.type;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.junit.Assert;


public class TestingDeclaredType extends TestingType implements DeclaredType
{
	private final List<TypeMirror> typeArgs;
	private final Element thisElement;
	
	public TestingDeclaredType(Element thisElement, TypeMirror... typeArgs)
	{
		super(TypeKind.DECLARED);
		this.thisElement = thisElement;
		this.typeArgs = Arrays.asList(typeArgs);
	}
	
	public TestingDeclaredType(TypeMirror... typeArgs)
	{
		this(null, typeArgs);
	}
	
	@Override
	public Element asElement()
	{
		if (this.thisElement == null) Assert.fail("Illegal method call");
		return this.thisElement;
	}
	
	@Override
	public TypeMirror getEnclosingType()
	{
		return null;
	}
	
	@Override
	public List<? extends TypeMirror> getTypeArguments()
	{
		return this.typeArgs;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitDeclared(this, p);
	}
	
}
