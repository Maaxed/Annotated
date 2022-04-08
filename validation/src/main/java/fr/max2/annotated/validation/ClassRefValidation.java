package fr.max2.annotated.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import fr.max2.annotated.processor.utils.ClassRef;

public class ClassRefValidation
{
	public static void validate()
	{
		checkFields(extractFields(ClassRef.class));
	}
	
	private static Stream<Field> extractFields(Class<?> clazz)
	{
		return Stream.of(clazz.getFields())
			.filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == String.class);
	}
	
	private static void checkFields(Stream<Field> fields)
	{
		fields.forEach(ClassRefValidation::checkField);
	}
	
	private static void checkField(Field field)
	{
		String className;
		try
		{
			className = (String)field.get(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not get value of field '" + field.getName() + "'", e);
		}
		
		try
		{
			Class.forName(className, false, ClassRefValidation.class.getClassLoader());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not find class '" + className + "' on classpath for field '" + field.getName() + "'", e);
		}
	}
}
