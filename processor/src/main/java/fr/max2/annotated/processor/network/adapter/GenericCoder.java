package fr.max2.annotated.processor.network.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

public class GenericCoder extends AdapterCoder
{
	private final String serializer;
	private final IParameterSupplier parameterCoder;
	private final List<AdapterCoder> argCoders;

	protected GenericCoder(ProcessingTools tools, TypeMirror fromType, TypeMirror toType, String serializer, IParameterSupplier parameterCoder, List<AdapterCoder> argCoders)
	{
		super(tools, fromType, toType);
		this.serializer = serializer;
		this.parameterCoder = parameterCoder;
		this.argCoders = argCoders;
	}

	@Override
	public void codeAdapterInstance(ICodeConsumer output)
	{
		SimpleParameterListBuilder builder = new SimpleParameterListBuilder();
		this.parameterCoder.pipe(builder);
		this.argCoders.stream().map(AdapterCoder::codeAdapterInstanceToString).forEach(builder::add);
		output.write(this.serializer);
		output.writeLine(".of(");
		builder.build(output);
		output.write(")");
	}

	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools, String fromTypeName, String toTypeName, String serializer, BiConsumer<TypeMirror, Builder> parameterProvider)
	{
		DeclaredType fromInterface = tools.types.asDeclared(tools.types.erasure(tools.elements.getTypeElement(fromTypeName).asType()));
		DeclaredType toInterface = tools.types.asDeclared(tools.types.erasure(tools.elements.getTypeElement(toTypeName).asType()));
		return new TypedDataHandler<>(tools, fromInterface, true, fieldType ->
		{
			Builder builder = builder(tools, fromInterface, toInterface, fieldType);
			parameterProvider.accept(fieldType, builder);
			return builder.build(serializer);
		});
	}

	public static AdapterCoder build(ProcessingTools tools, TypeMirror fromType, TypeMirror toType, String serializer, IParameterSupplier parameterCoder, List<AdapterCoder> argCoders)
	{
		if (tools.types.isSameType(fromType, toType))
		{
			// No need to translate if both types are the same
			return tools.adapterCoders.getIdentityAdapter().createCoder(fromType);
		}
		return new GenericCoder(tools, fromType, toType, serializer, parameterCoder, argCoders);
	}

	public static Builder builder(ProcessingTools tools, DeclaredType baseFromType, DeclaredType baseToType, TypeMirror fieldType)
	{
		return new Builder(tools, baseFromType, baseToType, fieldType);
	}

	public static class Builder implements IParameterConsumer
	{
		private final ProcessingTools tools;
		private final DeclaredType baseFromType;
		private final DeclaredType baseToType;
		private final TypeMirror actualFromType;
		private TypeMirror actualToType;
		private final SimpleParameterListBuilder params = new SimpleParameterListBuilder();
		private final List<AdapterCoder> argCoders = new ArrayList<>();

		private Builder(ProcessingTools tools, DeclaredType baseFromType, DeclaredType baseToType, TypeMirror fieldType)
		{
			this.tools = tools;
			this.baseFromType = baseFromType;
			this.baseToType = baseToType;
			this.actualFromType = fieldType;
			this.actualToType = baseToType;
		}

		public void setActualToType(TypeMirror actualToType)
		{
			this.actualToType = actualToType;
		}

		@Override
		public void add(String param)
		{
			this.params.add(param);
		}

		public void addCoder(AdapterCoder coder)
		{
			this.argCoders.add(coder);
		}

		public void addCoders(int expectedTypeArgCount, TypeMirror fromImpl, TypeMirror toImpl)
		{
			DeclaredType refinedFromType = this.tools.types.refineTo(this.actualFromType, this.baseFromType);
			if (refinedFromType == null)
				throw new IncompatibleTypeException("The type '" + this.actualFromType + "' is not a sub type of " + this.baseFromType);

			this.tools.types.requireTypeArguments(refinedFromType, expectedTypeArgCount);

			// Find sub coders
			for (TypeMirror arg : refinedFromType.getTypeArguments())
			{
				if (arg.getKind() == TypeKind.WILDCARD)
					throw new IncompatibleTypeException("Wildcards are not supported in type arguments: '" + refinedFromType + "'");

				this.argCoders.add(this.tools.adapterCoders.getCoder(arg));
			}

			this.actualToType = this.tools.types.replaceTypeArguments(this.tools.types.erasure(this.actualFromType), this.baseToType, i -> this.argCoders.get(i).typeTo);

			DeclaredType refinedToType = this.tools.types.refineTo(this.actualToType, this.baseToType);

			// Check implementation compatibility
			DeclaredType implFromType = this.tools.types.replaceTypeArguments(fromImpl, this.baseFromType, refinedFromType);
			this.tools.types.requireAssignable(implFromType, this.actualFromType);

			DeclaredType implToType = this.tools.types.replaceTypeArguments(toImpl, this.baseToType, refinedToType);
			this.tools.types.requireAssignable(implToType, this.actualToType);
		}

		public AdapterCoder build(String serializer)
		{
			return GenericCoder.build(this.tools, this.actualFromType, this.actualToType, serializer, this.params, this.argCoders);
		}
	}

	public static interface IImplementationProvider
	{
		TypeMirror findImplementation(TypeMirror type) throws CoderException;
	}
}
