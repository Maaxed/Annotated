package fr.max2.annotated.processor.model;

import java.io.IOException;

public class SimpleCodeBuilder implements ICodeConsumer, ICodeSupplier
{
	private final StringBuilder code;

	public SimpleCodeBuilder()
	{
		this.code = new StringBuilder();
	}

	@Override
	public void write(CharSequence code)
	{
		this.code.append(code);
	}

	@Override
	public void pipe(ICodeConsumer output) throws IOException
	{
		output.write(this.build());
	}
	
	public String build()
	{
		return this.code.toString();
	}
	
}
