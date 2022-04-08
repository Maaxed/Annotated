package fr.max2.annotated.processor.utils.exceptions;

import static fr.max2.annotated.processor.utils.exceptions.ExceptionUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.junit.Test;


public class ExceptionUtilsTest
{
	@Test
	public void testTryAndWrapIOExceptions()
	{
		tryAndWrapIOExceptions(() ->
		{
			String test = "NothingSpecial";
			test.toString();
		});
		
		assertThrows(UncheckedIOException.class, () ->
		tryAndWrapIOExceptions(() ->
		{
			throw new IOException("This is a test");
		}));
	}
	
	@Test
	public void testWrapIOExceptionsRunnable()
	{
		Runnable noException = wrapIOExceptions(() ->
		{
			String test = "NothingSpecial";
			test.toString();
		});
		noException.run();

		Runnable throwException = wrapIOExceptions(() ->
		{
			throw new IOException("This is a test");
		});
		assertThrows(UncheckedIOException.class, () -> throwException.run());
	}
	
	@Test
	public void testWrapIOExceptionsConsumer()
	{
		Consumer<String> noException = wrapIOExceptions((String value) ->
		{
			value.toString();
		});
		noException.accept("NothingSpecial");

		Consumer<String> throwException = wrapIOExceptions((String value) ->
		{
			if (value.equals("Throw"))
				throw new IOException("This is a test");
		});
		throwException.accept("NothingSpecial");
		
		assertThrows(UncheckedIOException.class, () -> throwException.accept("Throw"));
	}
	
}
