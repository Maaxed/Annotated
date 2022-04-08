package fr.max2.annotated.processor.utils.exceptions;


public abstract class CoderExcepetion extends Exception 
{
	public CoderExcepetion()
	{
		super();
	}
	
	public CoderExcepetion(String message)
	{
		super(message);
	}
	
	public CoderExcepetion(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public CoderExcepetion(Throwable cause)
	{
		super(cause);
	}
}
