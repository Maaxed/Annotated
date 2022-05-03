package fr.max2.annotated.processor.network.adapter;

import java.util.List;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.util.ProcessingTools;

public class IdentityCoder extends GenericCoder
{
	protected IdentityCoder(ProcessingTools tools, TypeMirror type, String serializer)
	{
		super(tools, type, type, serializer, params -> {}, List.of());
	}

	@Override
	public OutputExpressions code(String fieldName, String valueFromAccess, String valueToAccess, String ctxAccess)
	{
		return new OutputExpressions(valueFromAccess, valueToAccess);
	}

	public static ICoderHandler<AdapterCoder> handler(ProcessingTools tools)
	{
		return new TypedDataHandler<>(tools, tools.elements.objectElement.asType(), true, type -> new IdentityCoder(tools, type, "fr.max2.annotated.lib.network.adapter.IdentityAdapter"))
		{
			@Override
			public String toString()
			{
				return "IdentityHandler";
			}
		};
	}
}
