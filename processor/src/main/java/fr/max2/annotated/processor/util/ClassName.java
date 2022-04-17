package fr.max2.annotated.processor.util;


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
		return this.packageName + "." + this.shortName;
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
	public int hashCode()
	{
		return 31 * (31 + this.packageName.hashCode()) + this.shortName.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ClassName))
			return false;
		
		ClassName other = (ClassName)obj;
		return this.packageName.equals(other.packageName) && this.shortName.contains(other.shortName);
	}

	@Override
	public String toString()
	{
		return this.packageName + ":" + this.shortName;
	}
}
