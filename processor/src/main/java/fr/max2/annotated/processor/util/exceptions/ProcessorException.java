package fr.max2.annotated.processor.util.exceptions;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.util.ProcessingTools;

public class ProcessorException extends RuntimeException
{
	private final @Nullable Element ctxElement;
	private final Optional<? extends AnnotationMirror> ctxAnnotation;
	private final @Nullable String ctxAnnotationProperty;
	
	private ProcessorException(Builder builder, @Nullable String message, Throwable cause)
	{
		super(message, cause);
		this.ctxElement = builder.ctxElement;
		this.ctxAnnotation = builder.ctxAnnotation;
		this.ctxAnnotationProperty = builder.ctxAnnotationProperty;
	}

	private ProcessorException(Builder builder, @Nullable String message)
	{
		super(message);
		this.ctxElement = builder.ctxElement;
		this.ctxAnnotation = builder.ctxAnnotation;
		this.ctxAnnotationProperty = builder.ctxAnnotationProperty;
	}
	
	public void log(ProcessingTools tools)
	{
		if (this.ctxElement == null)
		{
			tools.log(Kind.ERROR, this.getMessage());
		}
		else if (this.ctxAnnotationProperty == null)
		{
			tools.log(Kind.ERROR, this.getMessage(), this.ctxElement, this.ctxAnnotation);
		}
		else
		{
			tools.log(Kind.ERROR, this.getMessage(), this.ctxElement, this.ctxAnnotation, this.ctxAnnotationProperty);
		}
		
		// Log cause
		Throwable cause = this.getCause();
		while (cause != null)
		{
			if (cause instanceof ProcessorException pe)
			{
				pe.log(tools);
				return;
			}
			cause = cause.getCause();
		}
		
		this.printStackTrace();
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private @Nullable Element ctxElement = null;
		private Optional<? extends AnnotationMirror> ctxAnnotation = Optional.empty();
		private @Nullable String ctxAnnotationProperty = null;
		
		private Builder()
		{ }
		
		public Builder context(Element ctxElement)
		{
			this.ctxElement = ctxElement;
			return this;
		}
		
		public Builder context(Element ctxElement, Optional<? extends AnnotationMirror> ctxAnnotation)
		{
			this.context(ctxElement);
			this.ctxAnnotation = ctxAnnotation;
			return this;
		}
		
		public Builder context(Element ctxElement, Optional<? extends AnnotationMirror> ctxAnnotation, String ctxAnnotationProperty)
		{
			this.context(ctxElement, ctxAnnotation);
			this.ctxAnnotationProperty = ctxAnnotationProperty;
			return this;
		}
		
		public ProcessorException build()
		{
			return build((String)null);
		}
		
		public ProcessorException build(@Nullable String message)
		{
			return new ProcessorException(this, message);
		}
		
		public ProcessorException build(@Nullable Throwable cause)
		{
			return build(null, cause);
		}
		
		public ProcessorException build(@Nullable String message, Throwable cause)
		{
			return new ProcessorException(this, message, cause);
		}
	}
}
