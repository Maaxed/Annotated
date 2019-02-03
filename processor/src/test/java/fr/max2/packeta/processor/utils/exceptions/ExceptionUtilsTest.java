package fr.max2.packeta.processor.utils.exceptions;

import static fr.max2.packeta.processor.utils.exceptions.ExceptionUtils.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ExceptionUtilsTest
{
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testTryAndWrapIOExceptions()
	{
		tryAndWrapIOExceptions(() ->
		{
			String test = "NothingSpecial";
			test.toString();
		});
		
		exception.expect(UncheckedIOException.class);
		tryAndWrapIOExceptions(() ->
		{
			throw new IOException("This is a test");
		});
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
		exception.expect(UncheckedIOException.class);
		throwException.run();
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
		
		exception.expect(UncheckedIOException.class);
		throwException.accept("Throw");
	}
	
}
