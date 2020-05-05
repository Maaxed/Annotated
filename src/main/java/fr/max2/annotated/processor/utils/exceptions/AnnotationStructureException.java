package fr.max2.annotated.processor.utils.exceptions;

public class AnnotationStructureException extends RuntimeException
{

	public AnnotationStructureException()
	{
		super();
	}
	
	public AnnotationStructureException(String message)
	{
		super(message);
	}
	
	public AnnotationStructureException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public AnnotationStructureException(Throwable cause)
	{
		super(cause);
	}

}