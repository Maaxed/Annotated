package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ClientPacket
{
	/**
	 * Indicate if the execution of the packet on the client side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the client Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	boolean runInClientThread() default true;
}
