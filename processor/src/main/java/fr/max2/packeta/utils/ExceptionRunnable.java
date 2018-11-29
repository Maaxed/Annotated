package fr.max2.packeta.utils;

@FunctionalInterface
public interface ExceptionRunnable<E extends Exception>
{
	void run() throws E;
}
