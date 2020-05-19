package fr.max2.annotated.processor.network.coder.handler;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;

public class SimpleDataHandler implements IDataHandler
{
	private final Predicate<TypeMirror> typeValidator;
	private final Supplier<DataCoder> coderProvider;
	
	public SimpleDataHandler(Predicate<TypeMirror> typeValidator, Supplier<DataCoder> coderProvider)
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
	public DataCoder createCoder()
	{
		return this.coderProvider.get();
	}
}
