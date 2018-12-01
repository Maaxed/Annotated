package fr.max2.packeta.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class ExceptionUtils
{
	public static void tryAndWrapIOExceptions(ExceptionRunnable<? extends IOException> action)
	{
		try
		{
			action.run();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
	
	public static Runnable wrapIOExceptions(ExceptionRunnable<? extends IOException> action)
	{
		return () -> tryAndWrapIOExceptions(action);
	}
	
	public static <T> Consumer<T> wrapIOExceptions(ExceptionConsumer<T, ? extends IOException> action)
	{
		return t ->
		{
			try
			{
				action.accept(t);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}
}
