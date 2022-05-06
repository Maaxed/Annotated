package fr.max2.annotated.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface NetworkSerializable
{
	/**
	 * Provides the name of the serializer class to generate.
	 * If the string is empty, the name of the class will automatically be generated from the name the name of the serializable class.
	 * @return the name of the serializer class to generate or an empty string
	 */
	String serializerClassName() default "";

	/**
	 * The way the serialized field are selected
	 */
	SelectionMode fieldSelectionMode() default SelectionMode.PUBLIC;
}
