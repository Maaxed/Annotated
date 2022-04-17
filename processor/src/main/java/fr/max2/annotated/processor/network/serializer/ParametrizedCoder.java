package fr.max2.annotated.processor.network.serializer;

import java.util.List;
import java.util.function.BiConsumer;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IParameterConsumer;
import fr.max2.annotated.processor.network.model.IParameterSupplier;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class ParametrizedCoder extends GenericCoder
{
	public ParametrizedCoder(ProcessingTools tools, TypeMirror type, String serializer, IParameterSupplier parameterCoder, List<SerializationCoder> parameterCoders)
	{
		super(tools, type, serializer, params ->
		{
			parameterCoder.pipe(params);
			parameterCoders.stream().map(SerializationCoder::codeSerializerInstance).forEach(params::add);
		});
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serilizer, BiConsumer<TypeMirror, IParameterConsumer> parameterCoder, ISubCoderProvider coderProvider)
	{
		return new NamedDataHandler<>(tools, typeName, (t, paramType) -> new ParametrizedCoder(tools, paramType, serilizer, params -> parameterCoder.accept(paramType, params), coderProvider.buildSubCoders(paramType)));
	}
	
	public static interface ISubCoderProvider
	{
		List<SerializationCoder> buildSubCoders(TypeMirror type) throws CoderException;
	}
}
