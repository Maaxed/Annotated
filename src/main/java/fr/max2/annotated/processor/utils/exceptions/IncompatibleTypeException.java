package fr.max2.annotated.processor.utils.exceptions;

public class IncompatibleTypeException extends CoderExcepetion
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