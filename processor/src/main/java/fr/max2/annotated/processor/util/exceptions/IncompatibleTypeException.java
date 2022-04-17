package fr.max2.annotated.processor.util.exceptions;

public class IncompatibleTypeException extends CoderException
{

	public IncompatibleTypeException()
	{
		super();
	}
	
	public IncompatibleTypeException(String message)
	{
		super(message);
	}
	
	public IncompatibleTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public IncompatibleTypeException(Throwable cause)
	{
		super(cause);
	}

}