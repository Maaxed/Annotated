package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.CoderCompatibility;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class SpecialDataHandler<C> implements ICoderHandler<C>
{
	private final TypeKind kind;
	private final ICoderProvider<C> coderProvider;
	
	public SpecialDataHandler(TypeKind kind, ICoderProvider<C> coderProvider)
	{
		this.kind = kind;
		this.coderProvider = coderProvider;
	}
	
	@Override
	public CoderCompatibility getCompatibilityFor(TypeMirror type)
	{
		return CoderCompatibility.matching(type.getKind() == this.kind);
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
