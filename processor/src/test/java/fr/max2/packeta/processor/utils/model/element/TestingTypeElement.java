package fr.max2.packeta.processor.utils.model.element;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.processor.utils.model.type.TestingNoType;


public class TestingTypeElement extends TestingQualifiedNameable implements TypeElement
{
	private final NestingKind nesting;
	private final List<TypeMirror> interfaces = new ArrayList<>();
	private final List<TypeParameterElement> typeParameters = new ArrayList<>();
	private TypeMirror superClass = TestingNoType.INSTANCE;
	
	public TestingTypeElement(ElementKind kind, NestingKind nesting, TypeMirror correspondingType, String qualifiedName)
	{
		super(kind, correspondingType, qualifiedName);
		this.nesting = nesting;
	}
	
	public void setSuperClass(TypeMirror superClass)
	{
		this.superClass = superClass;
	}
	
	public void addInterface(TypeMirror newInterface)
	{
		this.interfaces.add(newInterface);
	}
	
	public void addTypeParameter(TypeParameterElement parameter)
	{
		this.typeParameters.add(parameter);
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
