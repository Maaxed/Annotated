package fr.max2.packeta.processor.utils.exceptions;

@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception>
{
	void accept(T t) throws E;
}
