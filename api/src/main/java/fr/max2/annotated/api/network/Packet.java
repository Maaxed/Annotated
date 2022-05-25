package fr.max2.annotated.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each method annotated with this annotation will generate a corresponding packet class.
 * The packet can be sent from a client to the server with the 'sendToServer' method.
 * When the packet is received in the server, the method on which this annotation is on will be called using the arguments passed to 'sendToServer'.
 * <p>
 * The enclosing class of the annotated method should be annotated with a Channel annotation.
 * The generated packet will automatically be registered to the enclosing channel.
 * @see GenerateChannel
 * @see DelegateChannel
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Packet
{
	Destination value();
	
	/**
	 * Indicate if the execution of the packet on the server side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the main server Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	boolean runInMainThread() default true;

	public static enum Destination
	{
		CLIENT,
		SERVER
	}
}
