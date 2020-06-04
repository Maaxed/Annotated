package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class PrimitiveCoder
{
	public static final IHandlerProvider
		// Integers
		BYTE = provider("Byte"),
		SHORT = provider("Short"),
		INT = provider("Int"),
		LONG = provider("Long"),
		
		//Floats
		FLOAT = provider("Float"),
		DOUBLE = provider("Double"),
		
		// Other primitives
		BOOLEAN = provider("Boolean"),
		CHAR = provider("Char");
	
	private static IHandlerProvider provider(String name)
	{
		return (tools) -> new Handler(tools, name);
	}
	
	private static class Handler extends TypedDataHandler
	{
		private final String primitiveName;
		
		public Handler(ProcessingTools tools, String name)
		{
			super(tools, tools.types.getPrimitiveType(TypeKind.valueOf(name.toUpperCase())));
			this.primitiveName = name;
		}

		@Override
		public boolean canProcess(TypeMirror type)
		{
			return this.tools.types.isAssignable(type, this.type) && this.tools.types.isAssignable(this.type, type);
		}

		@Override
		public DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
		{
			return new DataCoderUtils.SimpleCoder(tools, uniqueName, paramType, properties, this.primitiveName);
		}
	}
}
