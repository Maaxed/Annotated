package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class PrimitiveCoder extends SerializationCoder
{
	private final String name;
	public PrimitiveCoder(ProcessingTools tools, TypeMirror type, String name)
	{
		super(tools, type);
		this.name = name;
	}

	@Override
	public String codeSerializerInstance()
	{
		return "fr.max2.annotated.lib.network.serializer.PrimitiveSerializer." + this.name + "Serializer.INSTANCE";
	}
	
	@Override
	public OutputExpressions code(String fieldName)
	{
		String serializer = this.codeSerializerInstance();
		return new OutputExpressions(
			serializer + ".encodePrimitive(buf, value." + fieldName + ")",
			serializer + ".decodePrimitive(buf)");
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String name)
	{
		return new TypedDataHandler<>(tools, tools.types.getPrimitiveType(TypeKind.valueOf(name.toUpperCase())))
		{
			@Override
			public boolean canProcess(TypeMirror type)
			{
				return this.tools.types.isAssignable(type, this.type) && this.tools.types.isAssignable(this.type, type);
			}

			@Override
			public SerializationCoder createCoder(ProcessingTools tools, TypeMirror paramType) throws CoderException
			{
				return new PrimitiveCoder(tools, paramType, name);
			}
		};
	}
}
