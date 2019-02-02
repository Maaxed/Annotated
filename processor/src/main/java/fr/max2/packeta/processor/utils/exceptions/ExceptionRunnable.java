package fr.max2.packeta.processor.utils.exceptions;

@FunctionalInterface
public interface ExceptionRunnable<E extends Exception>
{
	void run() throws E;
}
