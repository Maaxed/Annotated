package fr.max2.annotated.processor.util.exceptions;


public class RoundException extends RuntimeException
{
	public RoundException()
	{
		super();
	}

	public RoundException(String message)
	{
		super(message);
	}

	public RoundException(Throwable cause)
	{
		super(cause);
	}

	public RoundException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
