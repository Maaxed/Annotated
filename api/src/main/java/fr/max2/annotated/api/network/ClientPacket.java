package fr.max2.annotated.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each method annotated with this annotation will generate a corresponding packet class.
 * The packet can be sent from the server to a client with the 'sendTo' method.
 * When the packet is received in the client, the method on which this annotation is on will be called using the arguments passed to 'sendTo'.
 * <p>
 * The enclosing class of the annotated method should be annotated with a Channel annotation.
 * The generated packet will automatically be registered to the enclosing channel.
 * @see GenerateChannel
 * @see DelegateChannel
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface ClientPacket
{
	/**
	 * Provides the name of the packet class to generate.
	 * If the string is empty, the name of the class will automatically be generated from the name of the method and the name of the class.
	 * @return the name of the packet class to generate or an empty string
	 */
	String className() default "";
	
	/**
	 * Indicate if the execution of the packet on the client side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the main client Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	boolean runInMainThread() default true;
}
