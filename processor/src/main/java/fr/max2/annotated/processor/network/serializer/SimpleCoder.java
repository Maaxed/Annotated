package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.ICodeConsumer;
import fr.max2.annotated.processor.util.ProcessingTools;

public class SimpleCoder extends SerializationCoder
{
	protected final String serilizer;
	public SimpleCoder(ProcessingTools tools, TypeMirror type, String serilizer)
	{
		super(tools, type);
		this.serilizer = serilizer;
	}

	@Override
	public void codeSerializerInstance(ICodeConsumer output)
	{
		output.write(this.serilizer);
	}
	
	@Override
	public OutputExpressions code(String fieldName, String valueAccess)
	{
		String serializer = this.serilizer;
		return new OutputExpressions(
			serializer + ".encode(buf, " + valueAccess + ")",
			serializer + ".decode(buf)");
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serilizer)
	{
		return new NamedDataHandler<>(tools, typeName, false, paramType -> new SimpleCoder(tools, paramType, serilizer));
	}
}
