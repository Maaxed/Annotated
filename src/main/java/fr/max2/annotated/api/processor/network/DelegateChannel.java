package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DelegateChannel
{
	/**
	 * Provides the canonical name of the network class corresponding to the channel to use.
	 * The network class should be annotated with either {@link GenerateChannel} or {@link DelegateChannel}.
	 * @return the name of the network class
	 */
	String value();
}
