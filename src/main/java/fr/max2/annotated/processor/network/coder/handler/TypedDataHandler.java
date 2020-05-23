package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.ProcessingTools;

public abstract class TypedDataHandler implements IDataHandler
{
	protected TypeMirror type;
	protected ProcessingTools tools;

	@Override
	public void init(ProcessingTools tools)
	{
		this.tools = tools;
		this.type = findType();
	}
	
	protected abstract TypeMirror findType();
	
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
