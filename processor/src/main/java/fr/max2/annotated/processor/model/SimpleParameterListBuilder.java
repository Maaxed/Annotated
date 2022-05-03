package fr.max2.annotated.processor.model;

import java.util.ArrayList;
import java.util.List;

public class SimpleParameterListBuilder implements IParameterConsumer, IParameterSupplier
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
	
	public void build(ICodeConsumer output)
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
		this.build(code);
		return code.build();
	}
	
	public String buildSingleLine()
	{
		return String.join(", ", this.params);
	}
	
}
