package fr.max2.annotated.processor.network.coder.handler;

import java.util.function.Supplier;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;

public class NamedDataHandler extends TypedDataHandler
{
	public final String typeName;
	protected final Supplier<DataCoder> coderProvider;
	
	public NamedDataHandler(String typaName, Supplier<DataCoder> coderProvider)
	{
		this.typeName = typaName;
		this.coderProvider = coderProvider;
	}
	
	@Override
	protected TypeMirror findType()
	{
		return tools.types.erasure(this.tools.elements.getTypeElement(this.typeName).asType());
	}
	
	@Override
	public boolean canProcess(TypeMirror type)
	{
		return tools.types.isAssignable(type, this.type);
	}

	@Override
	public DataCoder createCoder()
	{
		return this.coderProvider.get();
	};
}
