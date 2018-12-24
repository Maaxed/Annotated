package fr.max2.packeta.utils;

public class Holder<T>
{
	protected T value;
	
	public Holder()
	{ }
	
	public Holder(T value)
	{
		this.value = value;
	}
	
	public T getValue()
	{
		return value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}
}
