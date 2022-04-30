package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.CoderCompatibility;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class TypedDataHandler<C> implements ICoderHandler<C>
{
	protected final TypeMirror type;
	protected final boolean inherited;
	protected final ICoderProvider<C> coderProvider;
	protected final ProcessingTools tools;

	public TypedDataHandler(ProcessingTools tools, TypeMirror type, boolean inherited, ICoderProvider<C> coderProvider)
	{
		this.tools = tools;
		this.type = type;
		this.inherited = inherited;
		this.coderProvider = coderProvider;
	}
	
	@Override
	public CoderCompatibility getCompatibilityFor(TypeMirror type)
	{
		switch (type.getKind())
		{
		case TYPEVAR:
		case WILDCARD:
		case UNION:
		case INTERSECTION:
			return CoderCompatibility.INCOMPATIBLE;
		
		default:
			if (!this.tools.types.isAssignable(type, this.type))
				return CoderCompatibility.INCOMPATIBLE;
			
			if (this.tools.types.isAssignable(this.type, type))
				return CoderCompatibility.EXACT_MATCH;
			
			return this.inherited ? CoderCompatibility.SUPER_TYPE_MATCH : CoderCompatibility.INCOMPATIBLE;
		}
	}

	@Override
	public C createCoder(TypeMirror paramType) throws CoderException
	{
		return this.coderProvider.createCoder(paramType);
	}
	
	@Override
	public String toString()
	{
		return "TypedHandler:" + this.type;
	}
}
