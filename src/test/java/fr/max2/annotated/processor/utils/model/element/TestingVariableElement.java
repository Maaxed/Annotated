package fr.max2.annotated.processor.utils.model.element;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


public class TestingVariableElement extends TestingElement implements VariableElement
{
	private Object compileTimeConstant = null;
	
	public TestingVariableElement(ElementKind kind, TypeMirror correspondingType, String name)
	{
		super(kind, correspondingType, name);
	}
	
	public void setCompileTypeConstant(Object compileTimeConstant)
	{
		this.compileTimeConstant = compileTimeConstant;
		this.addModifier(Modifier.FINAL);
	}
	
	@Override
	public Object getConstantValue()
	{
		return this.compileTimeConstant;
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitVariable(this, p);
	}
	
}
