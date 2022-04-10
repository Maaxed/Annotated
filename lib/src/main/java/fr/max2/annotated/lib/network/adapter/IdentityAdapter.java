package fr.max2.annotated.lib.network.adapter;

import net.minecraftforge.network.NetworkEvent.Context;

public class IdentityAdapter<T> implements NetworkAdapter<T, T>
{
	private static final IdentityAdapter<?> INSTANCE = new IdentityAdapter<>();
	
	private IdentityAdapter()
	{ }
	
	@SuppressWarnings("unchecked")
	public static <T> IdentityAdapter<T> of()
	{
		return (IdentityAdapter<T>)INSTANCE;
	}
	
	@Override
	public T toNetwork(T value)
	{
		return value;
	}

	@Override
	public T fromNetwork(T value, Context ctx)
	{
		return value;
	}
}
