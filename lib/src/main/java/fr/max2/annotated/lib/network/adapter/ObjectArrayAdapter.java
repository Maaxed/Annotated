package fr.max2.annotated.lib.network.adapter;

import java.util.function.IntFunction;

import net.minecraftforge.network.NetworkEvent.Context;

public class ObjectArrayAdapter<F, T> implements NetworkAdapter<F[], T[]>
{
	private final IntFunction<F[]> fromArrayConstructor;
	private final IntFunction<T[]> toArrayConstructor;
	private final NetworkAdapter<F, T> contentAdapter;

	private ObjectArrayAdapter(IntFunction<F[]> fromArrayConstructor, IntFunction<T[]> toArrayConstructor, NetworkAdapter<F, T> contentAdapter)
	{
		this.fromArrayConstructor = fromArrayConstructor;
		this.toArrayConstructor = toArrayConstructor;
		this.contentAdapter = contentAdapter;
	}
	
	public static <F, T> NetworkAdapter<F[], T[]> of(IntFunction<F[]> fromArrayConstructor, IntFunction<T[]> toArrayConstructor, NetworkAdapter<F, T> contentAdapter)
	{
		return new ObjectArrayAdapter<>(fromArrayConstructor, toArrayConstructor, contentAdapter);
	}

	@Override
	public T[] toNetwork(F[] value)
	{
		T[] res = this.toArrayConstructor.apply(value.length);

		for (int i = 0; i < value.length; i++)
		{
			res[i] = this.contentAdapter.toNetwork(value[i]);
		}
		
		return res;
	}

	@Override
	public F[] fromNetwork(T[] value, Context ctx)
	{
		F[] res = this.fromArrayConstructor.apply(value.length);

		for (int i = 0; i < value.length; i++)
		{
			res[i] = this.contentAdapter.fromNetwork(value[i], ctx);
		}
		
		return res;
	}
}
