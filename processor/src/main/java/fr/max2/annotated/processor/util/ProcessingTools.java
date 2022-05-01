package fr.max2.annotated.processor.util;

import java.util.Optional;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import fr.max2.annotated.processor.network.adapter.AdapterCoderFinder;
import fr.max2.annotated.processor.network.serializer.SerializerCoderFinder;
import fr.max2.annotated.processor.util.template.TemplateHelper;

public class ProcessingTools
{
	private final Messager messager;
	
	public final ExtendedElements elements;
	public final ExtendedTypes types;
	public final Filer filer;
	public final NamingUtils naming;
	public final SerializerCoderFinder serializerCoders;
	public final AdapterCoderFinder adapterCoders;
	public final TemplateHelper templates;
	
	public ProcessingTools(ProcessingEnvironment env)
	{
		this.messager = env.getMessager();
		this.filer = env.getFiler();
		this.elements = new ExtendedElements(this, env.getElementUtils());
		this.types = new ExtendedTypes(this, env.getTypeUtils());
		this.naming = new NamingUtils(this);
		this.serializerCoders = new SerializerCoderFinder(this);
		this.adapterCoders = new AdapterCoderFinder(this);
		this.templates = new TemplateHelper(this);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg)
	{
		this.messager.printMessage(kind, msg);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e)
	{
		this.messager.printMessage(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a)
	{
		if (a.isPresent())
			this.messager.printMessage(kind, msg, e, a.get());
		else
			this.log(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a, String property)
	{
		if (a.isPresent())
		{
			Optional<? extends AnnotationValue> value = this.elements.getAnnotationValue(a, property);
			if (value.isPresent())
			{
				this.messager.printMessage(kind, msg, e, a.get(), value.get());
			}
			else
			{
				this.messager.printMessage(kind, msg, e, a.get());
			}
		}
		else
		{
			this.log(kind, msg, e);
		}
	}
}
