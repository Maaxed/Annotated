package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class NamedDataHandler<C> extends TypedDataHandler<C>
{
	public final String typeName;
	protected final boolean inherited;
	protected final ICoderProvider<C> coderProvider;
	
	public NamedDataHandler(ProcessingTools tools, String typeName, boolean inherited, ICoderProvider<C> coderProvider)
	{
		super(tools, tools.types.erasure(tools.elements.getTypeElement(typeName).asType()));
		this.typeName = typeName;
		this.inherited = inherited;
		this.coderProvider = coderProvider;
	}
	
	@Override
	public boolean canProcess(TypeMirror type)
	{
		switch (type.getKind())
		{
		case TYPEVAR:
		case WILDCARD:
		case UNION:
		case INTERSECTION:
			return false;
		
		default:
			if (this.inherited)
				return this.tools.types.isAssignable(type, this.type);
			
			return this.tools.types.isAssignable(type, this.type) && this.tools.types.isAssignable(this.type, type);
		}
	}

	@Override
	public C createCoder(TypeMirror paramType) throws CoderException
	{
		return this.coderProvider.createCoder(paramType);
	};
	
	@Override
	public String toString()
	{
		return "NamedHandler:" + this.typeName;
	}
}
