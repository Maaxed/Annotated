package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface CustomData
{
	/**
	 * The type of data
	 * @return the DataType
	 */
	DataType type() default DataType.DEFAULT;
	
	/**
	 * The parameters for the DataHandler
	 * @return the parameters in a string array
	 */
	String[] value() default { }; //TODO [v2.0] create a property system
}
