package fr.max2.annotated.processor.network.adapter;

import java.util.Optional;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.CoderFinder;
import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;

public class AdapterCoderFinder extends CoderFinder<AdapterCoder>
{
	private final ICoderHandler<AdapterCoder> identityAdapter;
	public AdapterCoderFinder(ProcessingTools tools)
	{
		super(tools, "fr.max2.annotated.lib.network.adapter.NetworkAdapter");

		this.scanClass("fr.max2.annotated.lib.network.adapter.EntityAdapter");

		this.identityAdapter = IdentityCoder.handler(tools);
		this.handlers.add(ArrayCoder.handler(tools));
		this.handlers.add(CollectionCoder.handler(tools));
		this.handlers.add(MapCoder.handler(tools));
		TypeMirror optionalType = tools.elements.getTypeElement(Optional.class.getCanonicalName()).asType();
		this.handlers.add(GenericCoder.handler(tools, Optional.class.getCanonicalName(), Optional.class.getCanonicalName(), "fr.max2.annotated.lib.network.adapter.OptionalAdapter", (fieldType, builder) -> builder.addCoders(1, optionalType, optionalType)));

		this.handlers.add(GeneratedCoder.handler(tools));
	}

	@Override
	protected ICoderHandler<AdapterCoder> constantToHandler(VariableElement field, DeclaredType type)
	{
		this.tools.types.requireTypeArguments(type, 2);
		return SimpleCoder.handler(this.tools, this.tools.types.erasure(type.getTypeArguments().get(0)), this.tools.types.erasure(type.getTypeArguments().get(1)), field);
	}

	public ICoderHandler<AdapterCoder> getIdentityAdapter()
	{
		return this.identityAdapter;
	}

	@Override
	protected Optional<ICoderHandler<AdapterCoder>> getDefaultHandler(TypeMirror type)
	{
		return super.getDefaultHandler(type).or(() -> Optional.of(this.identityAdapter));
	}
}
