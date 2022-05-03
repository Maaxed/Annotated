package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;

public class PrimitiveCoder extends SimpleCoder
{
	public PrimitiveCoder(ProcessingTools tools, TypeMirror type, String name)
	{
		super(tools, type, "fr.max2.annotated.lib.network.serializer.PrimitiveSerializer." + name + "Serializer.INSTANCE");
	}
	
	@Override
	public OutputExpressions code(String fieldName, String valueAccess)
	{
		String serializer = this.serilizer;
		return new OutputExpressions(
			serializer + ".encodePrimitive(buf, " + valueAccess + ")",
			serializer + ".decodePrimitive(buf)");
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String name)
	{
		return new TypedDataHandler<>(tools, tools.types.getPrimitiveType(TypeKind.valueOf(name.toUpperCase())), false, type -> new PrimitiveCoder(tools, type, name));
	}
}
