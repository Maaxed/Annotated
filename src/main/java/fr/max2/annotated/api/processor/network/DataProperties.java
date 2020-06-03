package fr.max2.annotated.api.processor.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be used on a parameter of a method annotated with a Packet annotation or on a class used as a parameter.
 * You can configure the packet by adding properties to the {@link #value()} array.
 * <p>
 * A properties always use the following format :
 * {@code  "<identifier>=<value>"} where {@literal <identifier>} is the name of a valid property and {@code <value>} is a valid value for this property.
 * If {@code <identifier>} contains a dot, than the part before the dot is the group identifier and the part after the dot is the sub-property identifier.
 * <p>
 * For example the property {@code "content.key.type=STRING"} specifies that the 'type' sub-property of the 'key' sub-group of the 'content' group is set to {@code "STRING"}. 
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface DataProperties
{
	/**
	 * The list of properties for the DataCoder in the format: {@code  "<identifier>=<value>"} where {@literal <identifier>} is a valid property identifier and {@code <value>} is a valid value for this property
	 * @return the properties in a string array
	 */
	String[] value() default { };
}
