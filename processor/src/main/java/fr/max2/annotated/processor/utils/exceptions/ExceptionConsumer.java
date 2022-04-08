package fr.max2.annotated.processor.utils.exceptions;

@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception>
{
	void accept(T t) throws E;
}
