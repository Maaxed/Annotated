package fr.max2.annotated.processor.network.coder;


public enum CoderCompatibility
{
	INCOMPATIBLE,
	SUPER_TYPE_MATCH,
	EXACT_MATCH;
	
	public boolean isCompatible()
	{
		return this != INCOMPATIBLE;
	}
	
	public static CoderCompatibility matching(boolean match)
	{
		return match ? EXACT_MATCH : INCOMPATIBLE;
	}
}
