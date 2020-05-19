package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;

public class PrimitiveCoder
{
	public static final IDataHandler
		// Integers
		BYTE = new Handler("Byte"),
		SHORT = new Handler("Short"),
		INT = new Handler("Int"),
		LONG = new Handler("Long"),
		
		//Floats
		FLOAT = new Handler("Float"),
		DOUBLE = new Handler("Double"),
		
		// Other primitives
		BOOLEAN = new Handler("Boolean"),
		CHAR = new Handler("Char");
	
	private static class Handler extends TypedDataHandler
	{
		private final String primitiveName;
		private final TypeKind kind;
		
		public Handler(String name)
		{
			this.primitiveName = name;
			this.kind = TypeKind.valueOf(name.toUpperCase());
		}
		
		@Override
		protected TypeMirror findType()
		{
			return this.tools.types.getPrimitiveType(this.kind);
		}

		@Override
		public boolean canProcess(TypeMirror type)
		{
			return this.tools.types.isAssignable(type, this.type) && this.tools.types.isAssignable(this.type, type);
		}

		@Override
		public DataCoder createCoder()
		{
			return new DataCoderUtils.SimpleCoder(this.primitiveName);
		}
	}
}
