package fr.max2.annotated.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each class annotated with this annotation will generate a corresponding network class.
 * The network contains a new channel with the given name and version.
 * All packets generated from enclosing methods will automatically be registered to this channel.
 * @see DelegateChannel
 */
@Retention(RetentionPolicy.SOURCE)
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
