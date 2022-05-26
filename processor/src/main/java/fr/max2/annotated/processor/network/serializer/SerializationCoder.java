package fr.max2.annotated.processor.network.serializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.model.ICodeConsumer;
import fr.max2.annotated.processor.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.util.ProcessingTools;

public abstract class SerializationCoder
{
	public final ProcessingTools tools;
	public final TypeMirror type;
	
	public SerializationCoder(ProcessingTools tools, TypeMirror type)
	{
		this.tools = tools;
		this.type = type;
	}

	public abstract void codeSerializerInstance(ICodeConsumer output) throws IOException;
	
	public final String codeSerializerInstanceToString()
	{
		SimpleCodeBuilder code = new SimpleCodeBuilder();
		try
		{
			this.codeSerializerInstance(code);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return code.build();
	}
	
	public OutputExpressions code(String fieldName, String valueAccess)
	{
		return new OutputExpressions(
			"this." + fieldName + ".encode(buf, " + valueAccess + ")",
			"this." + fieldName + ".decode(buf)",
			Optional.of(new Field("fr.max2.annotated.lib.network.serializer.NetworkSerializer<" + this.type.toString() + ">", fieldName, this.codeSerializerInstanceToString())));
	}
	
	public static final class OutputExpressions
	{
		public final String encodeCode;
		public final String decodeCode;
		public final Optional<Field> field;

		public OutputExpressions(String encodeCode, String decodeCode, Optional<Field> field)
		{
			this.encodeCode = encodeCode;
			this.decodeCode = decodeCode;
			this.field = field;
		}

		public OutputExpressions(String encodeCode, String decodeCode)
		{
			this(encodeCode, decodeCode, Optional.empty());
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
