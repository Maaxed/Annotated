package fr.max2.annotated.processor.network.serializer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IParameterConsumer;
import fr.max2.annotated.processor.network.model.IParameterSupplier;
import fr.max2.annotated.processor.network.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.util.ProcessingTools;

public class GenericCoder extends SerializationCoder
{
	private final String serializer;
	private final IParameterSupplier parameterCoder;
	
	public GenericCoder(ProcessingTools tools, TypeMirror type, String serializer, IParameterSupplier parameterCoder)
	{
		super(tools, type);
		this.serializer = serializer;
		this.parameterCoder = parameterCoder;
	}

	@Override
	public String codeSerializerInstance()
	{
		SimpleParameterListBuilder builder = new SimpleParameterListBuilder();
		this.parameterCoder.pipe(builder);
		return this.serializer + ".of(" + builder.build() + ")";
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, BiConsumer<TypeMirror, IParameterConsumer> parameterCoder)
	{
		return new NamedDataHandler<>(tools, typeName, (t, fieldType) -> new GenericCoder(tools, fieldType, serializer, params -> parameterCoder.accept(fieldType, params)));
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, Function<TypeMirror, String> parameterCoder)
	{
		return handler(tools, typeName, serializer, (fieldType, params) -> params.add(parameterCoder.apply(fieldType)));
	}
}
