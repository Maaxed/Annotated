package fr.max2.annotated.lib.network.adapter;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;

public class DelegatedAdapter<F, T> implements NetworkAdapter<F, T>
{
	private final Function<F, T> toNetworkConverter;
	private final BiFunction<T, NetworkEvent.Context, F> fromNetworkConverter;
	
	public DelegatedAdapter(Function<F, T> toNetworkConverter, BiFunction<T, Context, F> fromNetworkConverter)
	{
		this.toNetworkConverter = toNetworkConverter;
		this.fromNetworkConverter = fromNetworkConverter;
	}

	@Override
	public T toNetwork(F value)
	{
		return this.toNetworkConverter.apply(value);
	}

	@Override
	public F fromNetwork(T value, NetworkEvent.Context ctx)
	{
		return this.fromNetworkConverter.apply(value, ctx);
	}
}
