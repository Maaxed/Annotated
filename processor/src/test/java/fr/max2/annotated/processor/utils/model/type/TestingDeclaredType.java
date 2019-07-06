package fr.max2.annotated.processor.utils.model.type;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.junit.Assert;

import fr.max2.annotated.processor.utils.model.element.TestingTypeElement;


public class TestingDeclaredType extends TestingType implements DeclaredType
{
	public static final TestingDeclaredType OBJECT = new TestingDeclaredType(new TestingTypeElement(ElementKind.CLASS, "java.lang.Object"));
	
	private final List<TypeMirror> typeArgs;
	private final Element thisElement;
	
	protected TestingDeclaredType(TypeKind kind, TestingTypeElement thisElement, TypeMirror... typeArgs)
	{
		super(kind);
		this.thisElement = thisElement;
		this.typeArgs = Arrays.asList(typeArgs);
		thisElement.setElementType(this);
	}
	
	public TestingDeclaredType(TestingTypeElement thisElement, TypeMirror... typeArgs)
	{
		this(TypeKind.DECLARED, thisElement, typeArgs);
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
