package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class NamedDataHandler extends TypedDataHandler
{
	public final String typeName;
	protected final IDataCoderProvider coderProvider;
	
	public NamedDataHandler(String typaName, IDataCoderProvider coderProvider)
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
	public DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		return this.coderProvider.createCoder(tools, uniqueName, paramType, properties);
	};
}
