package fr.max2.packeta.processor.utils.model.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.processor.utils.model.type.TestingDeclaredType;
import fr.max2.packeta.processor.utils.model.type.TestingNoType;


public class TestingTypeElement extends TestingQualifiedNameable implements TypeElement
{
	private NestingKind nesting = NestingKind.TOP_LEVEL;
	private final List<TypeMirror> interfaces = new ArrayList<>();
	private final List<TypeParameterElement> typeParameters = new ArrayList<>();
	private TypeMirror superClass = TestingNoType.NONE;
	
	public TestingTypeElement(ElementKind kind, String qualifiedName)
	{
		super(kind, null, qualifiedName);
	}
	
	public TestingTypeElement withSuperClass(TypeMirror superClass)
	{
		this.superClass = superClass;
		return this;
	}
	
	public TestingTypeElement withInterfaces(TypeMirror... newInterfaces)
	{
		this.interfaces.addAll(Arrays.asList(newInterfaces));
		return this;
	}
	
	public TestingTypeElement withNestingKind(NestingKind kind)
	{
		this.nesting = kind;
		return this;
	}
	
	public TestingTypeElement withNewTypeParameter(String name, TypeMirror... bounds)
	{
		this.typeParameters.add(new TestingTypeParameterElement(name, this, bounds));
		return this;
	}
	
	public void setElementType(TestingDeclaredType type)
	{
		this.correspondingType = type;
	}
	
	@Override
	public NestingKind getNestingKind()
	{
		return this.nesting;
	}
	
	@Override
	public TypeMirror getSuperclass()
	{
		return this.superClass;
	}
	
	@Override
	public List<? extends TypeMirror> getInterfaces()
	{
		return this.interfaces;
	}
	
	@Override
	public List<? extends TypeParameterElement> getTypeParameters()
	{
		return this.typeParameters;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitType(this, p);
	}
	
}
