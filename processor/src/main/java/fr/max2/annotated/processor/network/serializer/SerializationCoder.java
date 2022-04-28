package fr.max2.annotated.processor.network.serializer;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.network.model.ICodeConsumer;
import fr.max2.annotated.processor.network.model.SimpleCodeBuilder;
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

	public abstract void codeSerializerInstance(ICodeConsumer output);
	
	public final String codeSerializerInstanceToString()
	{
		SimpleCodeBuilder code = new SimpleCodeBuilder();
		this.codeSerializerInstance(code);
		return code.build();
	}
	
	public OutputExpressions code(String fieldName, String valueAccess)
	{
		SimpleCodeBuilder fieldCode = new SimpleCodeBuilder();
		this.codeSerializerInstance(fieldCode);
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
	
	public static void requireConcreteType(ProcessingTools tools, TypeMirror type) throws IncompatibleTypeException
	{
		Element elem = tools.types.asElement(type);
		if (elem == null)
			return; // Unknown type, assume it is concrete
		
		if (elem.getModifiers().contains(Modifier.ABSTRACT))
			throw new IncompatibleTypeException("The type '" + type + "' is abstract and cannot be instantiated");
	}
	
	public static void requireDefaultConstructor(ProcessingTools tools, TypeMirror type) throws IncompatibleTypeException
	{
		requireConstructor(tools, type, cons -> cons.getParameters().isEmpty());
	}
	
	public static void requireConstructor(ProcessingTools tools, TypeMirror type, List<TypeMirror> paramTypes) throws IncompatibleTypeException
	{
		requireConstructor(tools, type, isConstructorCompatible(tools, paramTypes));
	}
	
	public static void requireConstructor(ProcessingTools tools, TypeMirror type, Predicate<? super ExecutableElement> constructorFilter) throws IncompatibleTypeException
	{
		if (findConstructor(tools, type, constructorFilter) == null)
			throw new IncompatibleTypeException("The type '" + type + "' doesn't have the required constructor");
	}
	
	public static ExecutableElement findConstructor(ProcessingTools tools, TypeMirror type) throws IncompatibleTypeException
	{
		return findConstructor(tools, type, cons -> cons.getParameters().isEmpty());
	}
	
	public static ExecutableElement findConstructor(ProcessingTools tools, TypeMirror type, List<TypeMirror> paramTypes) throws IncompatibleTypeException
	{
		return findConstructor(tools, type, isConstructorCompatible(tools, paramTypes));
	}
	
	public static ExecutableElement findConstructor(ProcessingTools tools, TypeMirror type, Predicate<? super ExecutableElement> constructorFilter) throws IncompatibleTypeException
	{
		Element elem = tools.types.asElement(type);
		if (elem == null)
			throw new IncompatibleTypeException("The type '" + type + "' is not a DeclaredType");
		
		return ElementFilter.constructorsIn(elem.getEnclosedElements()).stream()
			.filter(constructorFilter)
			.reduce((a, b) -> { throw new IncompatibleTypeException("The type '" + elem + "' have multiple matching constructors"); })
			.orElse(null);
	}

	private static Predicate<? super ExecutableElement> isConstructorCompatible(ProcessingTools tools, List<TypeMirror> paramTypes)
	{
		return cons ->
		{
			List<? extends VariableElement> actualParams = cons.getParameters();
			if (actualParams.size() != paramTypes.size())
				return false;
			
			for (int i = 0; i < paramTypes.size(); i++)
			{
				if (!tools.types.isAssignable(actualParams.get(i).asType(), paramTypes.get(i)))
					return false;
			}
			
			return true;
		};
	}
}
