package fr.max2.annotated.processor.utils.exceptions;


public class TemplateException extends RuntimeException
{
	
	public TemplateException()
	{
		super();
	}
	
	public TemplateException(String message)
	{
		super(message);
	}
	
	public TemplateException(Throwable cause)
	{
		super(cause);
	}
	
	public TemplateException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public TemplateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
