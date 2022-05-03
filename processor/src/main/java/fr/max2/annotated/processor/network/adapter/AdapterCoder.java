package fr.max2.annotated.processor.network.adapter;

import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.model.ICodeConsumer;
import fr.max2.annotated.processor.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.util.ProcessingTools;

public abstract class AdapterCoder
{
	public final ProcessingTools tools;
	public final TypeMirror typeFrom;
	public final TypeMirror typeTo;

	public AdapterCoder(ProcessingTools tools, TypeMirror typeFrom, TypeMirror typeTo)
	{
		this.tools = tools;
		this.typeFrom = typeFrom;
		this.typeTo = typeTo;
	}

	public abstract void codeAdapterInstance(ICodeConsumer output);

	public final String codeAdapterInstanceToString()
	{
		SimpleCodeBuilder code = new SimpleCodeBuilder();
		this.codeAdapterInstance(code);
		return code.build();
	}

	public OutputExpressions code(String fieldName, String valueFromAccess, String valueToAccess, String ctxAccess)
	{
		return new OutputExpressions(
			"this." + fieldName + ".toNetwork(" + valueFromAccess + ")",
			"this." + fieldName + ".fromNetwork(" + valueToAccess + ", " + ctxAccess + ")",
			Optional.of(new Field("fr.max2.annotated.lib.network.adapter.NetworkAdapter<" + this.typeFrom.toString() + ", " + this.typeTo.toString() + ">", fieldName, this.codeAdapterInstanceToString())));
	}

	public static final class OutputExpressions
	{
		public final String toNetworkCode;
		public final String fromNetworkCode;
		public final Optional<Field> field;

		public OutputExpressions(String toNetworkCode, String fromNetworkCode, Optional<Field> field)
		{
			this.toNetworkCode = toNetworkCode;
			this.fromNetworkCode = fromNetworkCode;
			this.field = field;
		}

		public OutputExpressions(String toNetworkCode, String fromNetworkCode)
		{
			this(toNetworkCode, fromNetworkCode, Optional.empty());
		}
	}

	public static class Field
	{
		public final String type;
		public final String uniqueName;
		public final String initializationCode;

		public Field(String type, String uniqueName, String initializationCode)
		{
			this.type = type;
			this.uniqueName = uniqueName;
			this.initializationCode = initializationCode;
		}
	}
}
