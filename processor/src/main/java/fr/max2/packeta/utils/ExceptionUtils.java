package fr.max2.packeta.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class ExceptionUtils
{
	public static void tryRun(ExceptionRunnable<? extends IOException> action)
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
	
	public static Runnable toTryRun(ExceptionRunnable<? extends IOException> action)
	{
		return () -> tryRun(action);
	}
	
	public static <T> Consumer<T> toTryRun(ExceptionConsumer<T, ? extends IOException> action)
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
