package fr.max2.packeta.utils;

import java.util.function.Consumer;

public class StreamException extends RuntimeException
{
	
	public StreamException()
	{
		super();
	}
	
	public StreamException(String message)
	{
		super(message);
	}
	
	public StreamException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public StreamException(Throwable cause)
	{
		super(cause);
	}
	
	public static void tryRun(ExceptionRunnable<?> action)
	{
		try
		{
			action.run();
		}
		catch (Exception e)
		{
			throw new StreamException(e);
		}
	}
	
	public static Runnable toTryRun(ExceptionRunnable<?> action)
	{
		return () -> tryRun(action);
	}
	
	public static <T> Consumer<T> toTryRun(ExceptionConsumer<T, ?> action)
	{
		return t ->
		{
			try
			{
				action.accept(t);
			}
			catch (Exception e)
			{
				throw new StreamException(e);
			}
		};
	}
	
}
