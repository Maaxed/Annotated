package fr.max2.annotated.processor.network.serializer;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.model.ICodeConsumer;
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
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, TypeMirror targetType, VariableElement field)
	{
		return new TypedDataHandler<>(tools, targetType, false, paramType -> new SimpleCoder(tools, paramType, tools.elements.asTypeElement(field.getEnclosingElement()).getQualifiedName() + "." + field.getSimpleName()));
	}
}
