package fr.max2.packeta.processor.utils;

@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception>
{
	void accept(T t) throws E;
}
