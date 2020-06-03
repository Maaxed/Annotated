package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each class annotated with this annotation will generate a corresponding network class.
 * The network contains a channel which references the channel of the given network.
 * All packets generated from enclosing methods will automatically be registered to this channel.
 * @see GenerateChannel
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DelegateChannel
{
	/**
	 * Provides the canonical name of the network class corresponding to the channel to use.
	 * The network class should be annotated with {@link GenerateChannel}.
	 * @return the name of the network class
	 */
	String value();
}
