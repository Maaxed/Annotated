package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface DataProperties
{
	/**
	 * The list of properties for the DataHandler in the format: "identifier=value" where 'identifier' is a valid property identifier
	 * @return the properties in a string array
	 */
	String[] value() default { };
}
