package fr.max2.packeta.processor.utils.model.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


public class TestingExecutableElement extends TestingElement implements ExecutableElement
{
	private final TypeMirror receiverType, returnType;
	private final List<VariableElement> parameters;
	private boolean isVarArgs = false;
	private boolean isDefault = false;
	private AnnotationValue defaultValue = null;
	private final List<TypeMirror> exceptions = new ArrayList<>();
	private final List<TypeParameterElement> typeParameters = new ArrayList<>();
	
	public TestingExecutableElement(ElementKind kind, TypeMirror correspondingType, String name, TypeMirror receiverType, TypeMirror returnType, VariableElement... parameters)
	{
		super(kind, correspondingType, name);
		this.receiverType = receiverType;
		this.returnType = returnType;
		this.parameters = Arrays.asList(parameters);
	}
	
	public TestingExecutableElement withWarArgs()
	{
		this.isVarArgs = true;
		return this;
	}
	
	public TestingExecutableElement withDefaultModifier()
	{
		this.isDefault = true;
		this.addModifier(Modifier.DEFAULT);
		return this;
	}
	
	public TestingExecutableElement withDefaultValue(AnnotationValue defaultValue)
	{
		this.defaultValue = defaultValue;
		return this;
	}
	
	public void addThrownException(TypeMirror exception)
	{
		this.exceptions.add(exception);
	}
	
	public void addTypeParameter(TypeParameterElement parameter)
	{
		this.typeParameters.add(parameter);
	}
	
	@Override
	public List<? extends TypeParameterElement> getTypeParameters()
	{
		return this.typeParameters;
	}
	
	@Override
	public TypeMirror getReturnType()
	{
		return this.returnType;
	}
	
	@Override
	public List<? extends VariableElement> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public TypeMirror getReceiverType()
	{
		return this.receiverType;
	}
	
	@Override
	public boolean isVarArgs()
	{
		return this.isVarArgs;
	}
	
	@Override
	public boolean isDefault()
	{
		return this.isDefault;
	}
	
	@Override
	public List<? extends TypeMirror> getThrownTypes()
	{
		return this.exceptions;
	}
	
	@Override
	public AnnotationValue getDefaultValue()
	{
		return this.defaultValue;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitExecutable(this, p);
	}
	
}
