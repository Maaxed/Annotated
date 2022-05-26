package fr.max2.annotated.processor.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleParameterListBuilder implements IParameterConsumer, IParameterSupplier, ICodeSupplier
{
	List<String> params = new ArrayList<>();

	@Override
	public void add(String param)
	{
		this.params.add(param);
	}

	@Override
	public void pipe(IParameterConsumer output)
	{
		output.addAll(this.params);
	}

	@Override
	public void pipe(ICodeConsumer output) throws IOException
	{
		for (int i = 0; i < this.params.size(); i++)
		{
			output.write(this.params.get(i));
			if (i < this.params.size() - 1)
			{
				output.writeLine(",");
			}
			else
			{
				output.writeLine();
			}
		}
	}

	public String buildMultiLines()
	{
		SimpleCodeBuilder code = new SimpleCodeBuilder();
		try
		{
			this.pipe(code);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return code.build();
	}

	public String buildSingleLine()
	{
		return String.join(", ", this.params);
	}
}
