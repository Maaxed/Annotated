package fr.max2.annotated.processor.util.exceptions;


public class InvalidPropertyException extends CoderException
{

	public InvalidPropertyException()
	{
		super();
	}
	
	public InvalidPropertyException(String message)
	{
		super(message);
	}
	
	public InvalidPropertyException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public InvalidPropertyException(Throwable cause)
	{
		super(cause);
	}
	
}
