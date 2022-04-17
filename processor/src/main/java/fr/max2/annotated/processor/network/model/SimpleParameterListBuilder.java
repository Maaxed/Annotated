package fr.max2.annotated.processor.network.model;

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
	
	public String build()
	{
		return String.join(", ", this.params);
	}
	
}
