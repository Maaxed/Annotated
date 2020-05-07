package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface ServerPacket
{
	/**
	 * Indicate if the execution of the packet on the server side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the server Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	boolean runInServerThread() default true; //TODO [v2.0] add class name param
}
