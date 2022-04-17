package fr.max2.annotated.processor.network.model;

import java.util.Collection;
import java.util.List;

public interface IParameterConsumer
{
	void add(String param);
	
	default void addAll(String... params)
	{
		this.addAll(List.of(params));
	}
	
	default void addAll(Collection<String> params)
	{
		params.forEach(this::add);
	}
}
