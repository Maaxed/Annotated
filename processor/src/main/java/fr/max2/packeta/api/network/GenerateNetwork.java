package fr.max2.packeta.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenerateNetwork
{
	/**
	 * The name of the network. If left empty, the name will be the modid or the name of the enclosing class
	 * @return the network name
	 */
	String value() default "";
	
	/**
	 * The name of the generated class. If left empty, the name will be the name of the enclosing class suffixed with "Network"
	 * @return the name of the generated class
	 */
	String className() default "";
}
