package fr.max2.annotated.processor.network.serializer;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public abstract class SerializationCoder
{
	public final ProcessingTools tools;
	public final TypeMirror type;
	
	public SerializationCoder(ProcessingTools tools, TypeMirror type)
	{
		this.tools = tools;
		this.type = type;
	}

	public abstract String codeSerializerInstance();
	
	public OutputExpressions code(String fieldName)
	{
		return new OutputExpressions(
			"this." + fieldName + ".encode(buf, value." + fieldName + ")",
			"this." + fieldName + ".decode(buf)",
			Optional.of(new Field("fr.max2.annotated.lib.network.serializer.NetworkSerializer<" + this.type.toString() + ">", fieldName, this.codeSerializerInstance())));
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
	
	public static void requireDefaultConstructor(Types typeHelper, TypeMirror type, @Nullable String errorHelpInfo) throws IncompatibleTypeException
	{
		Element elem = typeHelper.asElement(type);
		if (elem == null) return; // Unknown type, assume it has a default constructor
		
		errorHelpInfo = errorHelpInfo == null ? "" : ". " + errorHelpInfo;
		
		if (elem.getModifiers().contains(Modifier.ABSTRACT)) throw new IncompatibleTypeException("The type '" + type + "' is abstract and can't be instantiated" + errorHelpInfo);
		
		if (!ElementFilter.constructorsIn(elem.getEnclosedElements()).stream().anyMatch(cons -> cons.getParameters().isEmpty())) throw new IncompatibleTypeException("The type '" + type + "' doesn't have a default constructor" + errorHelpInfo);
	}
}
