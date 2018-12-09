package fr.max2.packeta.utils;


public enum ValueInitStatus
{
	UNDEFINED,
	DECLARED,
	INITIALISED;
	
	public boolean isDeclared()
	{
		return this != UNDEFINED;
	}
	
	public boolean isInitialised()
	{
		return this == INITIALISED;
	}
	
}
