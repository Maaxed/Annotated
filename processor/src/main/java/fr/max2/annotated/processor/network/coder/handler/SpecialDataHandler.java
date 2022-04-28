package fr.max2.annotated.processor.network.coder.handler;

import javax.annotation.Nullable;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.exceptions.CoderException;

public class SpecialDataHandler<C> implements ICoderHandler<C>
{
	private final @Nullable TypeKind kind;
	private final ICoderProvider<C> coderProvider;
	
	public SpecialDataHandler(@Nullable TypeKind kind, ICoderProvider<C> coderProvider)
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
	public C createCoder(TypeMirror paramType) throws CoderException
	{
		return this.coderProvider.createCoder(paramType);
	}
	
	@Override
	public String toString()
	{
		return "SpecialHandler:" + this.kind;
	}
}
