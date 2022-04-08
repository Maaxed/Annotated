package fr.max2.annotated.processor.utils.exceptions;

@FunctionalInterface
public interface ExceptionRunnable<E extends Exception>
{
	void run() throws E;
}
