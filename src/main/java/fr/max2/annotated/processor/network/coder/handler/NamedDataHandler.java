package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class NamedDataHandler extends TypedDataHandler
{
	public final String typeName;
	protected final IDataCoderProvider coderProvider;
	
	public NamedDataHandler(ProcessingTools tools, String typeName, IDataCoderProvider coderProvider)
	{
		super(tools, tools.types.erasure(tools.elements.getTypeElement(typeName).asType()));
		this.typeName = typeName;
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
			return this.tools.types.isAssignable(type, this.type);
		}
	}

	@Override
	public DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws CoderExcepetion
	{
		return this.coderProvider.createCoder(tools, uniqueName, paramType, properties);
	};
	
	@Override
	public String toString()
	{
		return "NamedHandler:" + this.typeName;
	}
	
	public static IHandlerProvider provider(String typeName, IDataCoderProvider coderProvider)
	{
		return tools -> new NamedDataHandler(tools, typeName, coderProvider);
	}
}
