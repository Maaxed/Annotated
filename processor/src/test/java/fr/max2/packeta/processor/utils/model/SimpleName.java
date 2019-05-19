package fr.max2.packeta.processor.utils.model;

import javax.lang.model.element.Name;


public class SimpleName implements Name
{
	private final String value;
	
	public SimpleName(String value)
	{
		this.value = value;
	}
	
	@Override
	public int length()
	{
		return this.value.length();
	}
	
	@Override
	public char charAt(int index)
	{
		return this.value.charAt(index);
	}
	
	@Override
	public CharSequence subSequence(int start, int end)
	{
		return this.value.subSequence(start, end);
	}
	
	@Override
	public boolean contentEquals(CharSequence cs)
	{
		return this.value.contentEquals(cs);
	}
	
	@Override
	public String toString()
	{
		return this.value;
	}
	
}
