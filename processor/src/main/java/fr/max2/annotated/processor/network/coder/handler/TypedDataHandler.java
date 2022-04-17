package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.ProcessingTools;

public abstract class TypedDataHandler<C> implements ICoderHandler<C>
{
	protected final TypeMirror type;
	protected final ProcessingTools tools;

	public TypedDataHandler(ProcessingTools tools, TypeMirror type)
	{
		this.tools = tools;
		this.type = type;
	}
	
	public TypeMirror getType()
	{
		return this.type;
	}
	
	@Override
	public String toString()
	{
		return "TypedHandler:" + this.type;
	}
}
