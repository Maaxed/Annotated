package fr.max2.annotated.processor.util.exceptions;


public abstract class CoderException extends Exception 
{
	public CoderException()
	{
		super();
	}
	
	public CoderException(String message)
	{
		super(message);
	}
	
	public CoderException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public CoderException(Throwable cause)
	{
		super(cause);
	}
}
