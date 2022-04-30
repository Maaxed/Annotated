package fr.max2.annotated.api.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
public @interface IncludeField
{
	public String getter() default "";
}
