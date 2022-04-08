package fr.max2.annotated.processor.utils.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import org.junit.Assert;


public class TestingExecutableType extends TestingType implements ExecutableType
{
	private final TypeMirror receiverType;
	private final TypeMirror returnType;
	private final List<TypeMirror> parameters;
	private final List<TypeMirror> exceptions = new ArrayList<>();
	private final List<TypeVariable> typeVariables = new ArrayList<>();
	
	public TestingExecutableType(TypeMirror receiverType, TypeMirror returnType, TypeMirror... parameters)
	{
		super(TypeKind.EXECUTABLE);
		this.receiverType = receiverType;
		this.returnType = returnType;
		this.parameters = Arrays.asList(parameters);
	}
	
	public void addThrownException(TypeMirror exception)
	{
		this.exceptions.add(exception);
	}
	
	public void addTypeVariable(TypeVariable typeVar)
	{
		this.typeVariables.add(typeVar);
	}
	
	@Override
	public List<? extends TypeVariable> getTypeVariables()
	{
		return this.typeVariables;
	}
	
	@Override
	public TypeMirror getReturnType()
	{
		if (this.returnType == null) Assert.fail("Illegal method call");
		return this.returnType;
	}
	
	@Override
	public List<? extends TypeMirror> getParameterTypes()
	{
		return this.parameters;
	}
	
	@Override
	public TypeMirror getReceiverType()
	{
		if (this.receiverType == null) Assert.fail("Illegal method call");
		return this.receiverType;
	}
	
	@Override
	public List<? extends TypeMirror> getThrownTypes()
	{
		return this.exceptions;
	}
	
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitExecutable(this, p);
	}
	
}
