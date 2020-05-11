package fr.max2.annotated.processor.utils;


public class ClassName
{
	private String shortName;
	private String packageName;
	
	public ClassName(String packageName, String shortName)
	{
		this.packageName = packageName;
		this.shortName = shortName;
	}

	public String qualifiedName()
	{
		return packageName + "." + shortName;
	}
	
	public String shortName()
	{
		return this.shortName;
	}
	
	public String packageName()
	{
		return this.packageName;
	}
	
	@Override
	public String toString()
	{
		return this.packageName + ":" + this.shortName;
	}
}
