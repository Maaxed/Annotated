package fr.max2.annotated.processor.network.coder.handler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class SimpleDataHandler implements IDataHandler
{
	private final Predicate<TypeMirror> typeValidator;
	private final IDataCoderProvider coderProvider;
	
	public SimpleDataHandler(Predicate<TypeMirror> typeValidator, IDataCoderProvider coderProvider)
	{
		this.typeValidator = typeValidator;
		this.coderProvider = coderProvider;
	}
	
	@Override
	public boolean canProcess(TypeMirror type)
	{
		return this.typeValidator.test(type);
	}
	
	@Override
	public DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		return this.coderProvider.createCoder(tools, uniqueName, paramType, properties);
	}
}
