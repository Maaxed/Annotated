package fr.max2.autodata.utils;

@FunctionalInterface
public interface ExceptionRunnable<E extends Exception>
{
	void run() throws E;
}
