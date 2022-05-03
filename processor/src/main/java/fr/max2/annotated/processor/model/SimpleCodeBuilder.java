package fr.max2.annotated.processor.model;


public class SimpleCodeBuilder implements ICodeConsumer, ICodeSupplier
{
	private final StringBuilder code;

	public SimpleCodeBuilder()
	{
		this.code = new StringBuilder();
	}

	@Override
	public void write(String code)
	{
		this.code.append(code);
	}

	@Override
	public void pipe(ICodeConsumer output)
	{
		output.write(this.build());
	}
	
	public String build()
	{
		return this.code.toString();
	}
	
}
