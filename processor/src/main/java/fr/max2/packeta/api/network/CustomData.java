package fr.max2.packeta.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface CustomData
{
	DataType type() default DataType.CUSTOM;
	
	String[] value() default { }; //parameters
}
