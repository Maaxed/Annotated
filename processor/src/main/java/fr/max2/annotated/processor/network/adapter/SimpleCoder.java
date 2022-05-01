package fr.max2.annotated.processor.network.adapter;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.network.model.ICodeConsumer;
import fr.max2.annotated.processor.util.ProcessingTools;

public class SimpleCoder extends AdapterCoder
{
	protected final String serilizer;
	public SimpleCoder(ProcessingTools tools, TypeMirror typeFrom, TypeMirror typeTo, String serilizer)
	{
		super(tools, typeFrom, typeTo);
		this.serilizer = serilizer;
	}

	@Override
	public void codeAdapterInstance(ICodeConsumer output)
	{
		output.write(this.serilizer);
	}

	@Override
	public OutputExpressions code(String fieldName, String valueAccess, String ctxAccess)
	{
		String serializer = this.serilizer;
		return new OutputExpressions(
			serializer + ".toNetwork(" + valueAccess + ")",
			serializer + ".fromNetwork(" + valueAccess + ", " + ctxAccess + ")");
	}

	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools, TypeMirror targetFromType, TypeMirror targetToType, VariableElement field)
	{
		return new TypedDataHandler<>(tools, targetFromType, false, paramType -> new SimpleCoder(tools, paramType, targetToType, tools.elements.asTypeElement(field.getEnclosingElement()).getQualifiedName() + "." + field.getSimpleName()));
	}
}
