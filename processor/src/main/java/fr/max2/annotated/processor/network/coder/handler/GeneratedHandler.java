package fr.max2.annotated.processor.network.coder.handler;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.CoderCompatibility;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public class GeneratedHandler<C> implements ICoderHandler<C>
{
	private final ProcessingTools tools;
	private final Class<? extends Annotation> annotationType;
	private final ICoderProvider<C> coderProvider;

	public GeneratedHandler(ProcessingTools tools, Class<? extends Annotation> annotationType, ICoderProvider<C> coderProvider)
	{
		this.tools = tools;
		this.annotationType = annotationType;
		this.coderProvider = coderProvider;
	}

	@Override
	public CoderCompatibility getCompatibilityFor(TypeMirror type)
	{
		Element elem = this.tools.types.asElement(type);
		return CoderCompatibility.matching(elem != null && elem.getAnnotation(this.annotationType) != null);
	}

	@Override
	public C createCoder(TypeMirror paramType) throws CoderException
	{
		return this.coderProvider.createCoder(paramType);
	}

	@Override
	public String toString()
	{
		return "GeneratedHandler:" + this.annotationType.getCanonicalName();
	}
}
