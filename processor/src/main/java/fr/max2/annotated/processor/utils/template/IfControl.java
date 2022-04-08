package fr.max2.annotated.processor.utils.template;


public enum IfControl implements ITemplateControl
{
	TRUE(true),
	FALSE(false);

	private final boolean shouldPrint;
	
	private IfControl(boolean shouldPrint)
	{
		this.shouldPrint = shouldPrint;
	}
	
	@Override
	public boolean shouldPrint()
	{
		return this.shouldPrint;
	}
	
}
