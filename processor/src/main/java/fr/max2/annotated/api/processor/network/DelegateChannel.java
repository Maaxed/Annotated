package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each class annotated with this annotation will generate a corresponding network class.
 * The channel of the generated network is a reference to the channel of the given network.
 * All packets generated from enclosing methods will automatically be registered to this channel.
 * @see GenerateChannel
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DelegateChannel
{
	/**
	 * Provides the network class defining the channel to use.
	 * The network class should be annotated with {@link GenerateChannel}.
	 * @return the network class
	 */
	Class<?> value();
}
