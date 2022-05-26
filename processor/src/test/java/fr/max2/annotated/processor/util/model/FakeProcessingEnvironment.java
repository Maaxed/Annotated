package fr.max2.annotated.processor.util.model;

import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class FakeProcessingEnvironment implements ProcessingEnvironment
{
	public Messager messager = null;
	public Elements elements = null;
	public Types types = null;
	public Filer filer = null;

    @Override
    public Map<String, String> getOptions()
    {
        return null;
    }
    @Override
    public Messager getMessager()
    {
        return this.messager;
    }
    @Override
    public Filer getFiler()
    {
        return this.filer;
    }
    @Override
    public Elements getElementUtils()
    {
        return this.elements;
    }
    @Override
    public Types getTypeUtils()
    {
        return this.types;
    }
    @Override
    public SourceVersion getSourceVersion()
    {
        return null;
    }
    @Override
    public Locale getLocale()
    {
        return null;
    }    
}
