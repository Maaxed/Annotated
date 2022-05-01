package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.network.model.ICodeConsumer;
import fr.max2.annotated.processor.network.model.IParameterConsumer;
import fr.max2.annotated.processor.network.model.IParameterSupplier;
import fr.max2.annotated.processor.network.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class GenericCoder extends SerializationCoder
{
	private final String serializer;
	private final IParameterSupplier parameterCoder;
	private final List<SerializationCoder> argCoders;
	
	protected GenericCoder(ProcessingTools tools, TypeMirror type, String serializer, IParameterSupplier parameterCoder, List<SerializationCoder> argCoders)
	{
		super(tools, type);
		this.serializer = serializer;
		this.parameterCoder = parameterCoder;
		this.argCoders = argCoders;
	}

	@Override
	public void codeSerializerInstance(ICodeConsumer output)
	{
		SimpleParameterListBuilder builder = new SimpleParameterListBuilder();
		this.parameterCoder.pipe(builder);
		this.argCoders.stream().map(SerializationCoder::codeSerializerInstanceToString).forEach(builder::add);
		output.write(this.serializer);
		output.writeLine(".of(");
		builder.build(output);
		output.write(")");
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, BiConsumer<TypeMirror, Builder> parameterProvider)
	{
		DeclaredType interfaceType = tools.types.asDeclared(tools.types.erasure(tools.elements.getTypeElement(typeName).asType()));
		return new TypedDataHandler<>(tools, interfaceType, true, fieldType ->
		{
			Builder builder = builder(tools, interfaceType, fieldType);
			parameterProvider.accept(fieldType, builder);
			return builder.build(serializer);
		});
	}
	
	public static ICoderHandler<SerializationCoder> handler(ProcessingTools tools, String typeName, String serializer, Function<TypeMirror, String> parameterCoder)
	{
		return handler(tools, typeName, serializer, (fieldType, builder) -> builder.add(parameterCoder.apply(fieldType)));
	}
	
	public static Builder builder(ProcessingTools tools, DeclaredType baseType, TypeMirror fieldType)
	{
		return new Builder(tools, baseType, fieldType);
	}
	
	public static class Builder implements IParameterConsumer
	{
		private final ProcessingTools tools;
		private final DeclaredType baseType;
		private final TypeMirror fieldType;
		private final SimpleParameterListBuilder params = new SimpleParameterListBuilder();
		private final List<SerializationCoder> argCoders = new ArrayList<>();

		private Builder(ProcessingTools tools, DeclaredType baseType, TypeMirror fieldType)
		{
			this.tools = tools;
			this.baseType = baseType;
			this.fieldType = fieldType;
		}

		@Override
		public void add(String param)
		{
			this.params.add(param);
		}
		
		public void addCoder(SerializationCoder coder)
		{
			this.argCoders.add(coder);
		}
		
		public void addCoders(int expectedTypeArgCount, TypeMirror rawImplType)
		{
			DeclaredType refinedType = this.tools.types.refineTo(this.fieldType, this.baseType);
			if (refinedType == null)
				throw new IncompatibleTypeException("The type '" + this.fieldType + "' is not a sub type of " + this.baseType);

			this.tools.types.requireTypeArguments(refinedType, expectedTypeArgCount);
			
			for (TypeMirror arg : refinedType.getTypeArguments())
			{
				if (arg.getKind() == TypeKind.WILDCARD)
					throw new IncompatibleTypeException("Wildcards are not supported in type arguments: '" + refinedType + "'");
				
				this.argCoders.add(this.tools.serializerCoders.getCoder(arg));
			}
			
			DeclaredType implType = this.tools.types.replaceTypeArguments(rawImplType, this.baseType, refinedType);
			this.tools.types.requireAssignable(implType, this.fieldType);
		}
		
		public GenericCoder build(String serializer)
		{
			return new GenericCoder(this.tools, this.fieldType, serializer, this.params, this.argCoders);
		}
	}
	
	public static interface IImplementationProvider
	{
		TypeMirror findImplementation(TypeMirror type) throws CoderException;
	}
}
