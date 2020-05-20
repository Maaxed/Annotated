package fr.max2.annotated.processor.network.coder.handler;

import javax.annotation.Nullable;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class SpecialDataHandler implements IDataHandler
{
	private final @Nullable TypeKind kind;
	private final IDataCoderProvider coderProvider;
	
	public SpecialDataHandler(@Nullable TypeKind kind, IDataCoderProvider coderProvider)
	{
		this.kind = kind;
		this.coderProvider = coderProvider;
	}
	
	@Override
	public boolean canProcess(TypeMirror type)
	{
		return this.kind != null && type.getKind() == this.kind;
	}
	
	@Override
	public DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		return this.coderProvider.createCoder(tools, uniqueName, paramType, properties);
	}
	
	@Override
	public String toString()
	{
		return "SpecialHandler:" + this.kind;
	}
}
