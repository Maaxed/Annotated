package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface GenerateChannel
{
	/**
	 * Provides the name of the channel to generate.
	 * If the string is empty, the name of the channel will automatically be generated from the modid and the name of the class.
	 * @return the name of the channel to generate or an empty string
	 */
	String channelName() default "";
	
	/**
	 * Provides the version of the channel to generate
	 * @return the version of the channel
	 */
	String protocolVersion();
}
