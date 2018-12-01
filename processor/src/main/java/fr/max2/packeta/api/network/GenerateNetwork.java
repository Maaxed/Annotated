package fr.max2.packeta.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenerateNetwork
{
	String value() default ""; //networks name
	
	String className() default "";
}
