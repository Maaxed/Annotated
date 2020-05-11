package fr.max2.annotated.processor.network.model;

import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.TypeElement;

import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;

public class SimpleImportClassBuilder<B extends IImportClassBuilder<B>> implements IImportClassBuilder<B>
{
	public final Set<String> imports = new TreeSet<>();

	protected final ProcessingTools tools;
	protected final String packageName;

	public SimpleImportClassBuilder(ProcessingTools tools, String packageName)
	{
		this.tools = tools;
		this.packageName = packageName;
	}
	
	@Override
	public B addImport(String className)
	{
		return addImport(this.tools.elements.getTypeElement(className));
	}

	@Override
	public B addImport(ClassName className)
	{
		if (this.shouldImport(className))
		{
			this.imports.add(className.qualifiedName());
		}
		return self();
	}

	@Override
	public B addImport(TypeElement classElem)
	{
		if (this.shouldImport(classElem))
		{
			this.imports.add(classElem.getQualifiedName().toString());
		}
		return self();
	}
	
	protected boolean shouldImport(TypeElement classElem)
	{
		return shouldImport(this.tools.naming.buildClassName(classElem));
	}
	
	protected boolean shouldImport(ClassName className)
	{
		String packageName = className.packageName();
		
		if (packageName.startsWith(ClassRef.MAIN_PACKAGE + ".lib"))
			return true;
		
		if (packageName.startsWith("java.lang"))
			return false;
		
		if (packageName.equals(this.packageName) && !className.shortName().contains("."))
		{
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected B self()
	{
		return (B)this;
	}
}
