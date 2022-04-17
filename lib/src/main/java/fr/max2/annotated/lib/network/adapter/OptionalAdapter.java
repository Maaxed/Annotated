package fr.max2.annotated.lib.network.adapter;

import java.util.Optional;

import net.minecraftforge.network.NetworkEvent.Context;

public class OptionalAdapter<F, T> implements NetworkAdapter<Optional<F>, Optional<T>>
{
	private final NetworkAdapter<F, T> contentAdapter;

	private OptionalAdapter(NetworkAdapter<F, T> contentAdapter)
	{
		this.contentAdapter = contentAdapter;
	}

	public static <F, T> NetworkAdapter<Optional<F>, Optional<T>> of(NetworkAdapter<F, T> contentAdapter)
	{
		return new OptionalAdapter<>(contentAdapter);
	}

	@Override
	public Optional<T> toNetwork(Optional<F> value)
	{
		return value.map(this.contentAdapter::toNetwork);
	}

	@Override
	public Optional<F> fromNetwork(Optional<T> value, Context ctx)
	{
		return value.map(v -> this.contentAdapter.fromNetwork(v, ctx));
	}
}
