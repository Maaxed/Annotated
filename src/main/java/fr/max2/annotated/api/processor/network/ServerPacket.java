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
	 * Provides the name of the packet class to generate
	 * If the string is empty, the name of the class will automatically be generated from the name of the method and the name of the class
	 * @return the name of the packet class to generate or an empty string
	 */
	String className() default "";
	
	/**
	 * Indicate if the execution of the packet on the server side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the main server Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	boolean runInMainThread() default true;
}
