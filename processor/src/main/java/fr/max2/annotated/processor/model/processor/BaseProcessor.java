package fr.max2.annotated.processor.model.processor;

import java.util.Set;

import fr.max2.annotated.processor.util.ProcessingTools;

public abstract class BaseProcessor implements IProcessor
{
	protected final Set<Class<?>> supportedAnnotation;
	protected ProcessingTools tools;

	public BaseProcessor(Class<?>... supportedAnnotations)
	{
		this.supportedAnnotation = Set.of(supportedAnnotations);
	}

	@Override
	public Set<? extends Class<?>> getSupportedAnnotations()
	{
		return this.supportedAnnotation;
	}

	@Override
	public void init(ProcessingTools tools)
	{
		this.tools = tools;
	}
}
