package fr.max2.annotated.lib.network.adapter;

import java.util.Collection;
import java.util.function.IntFunction;

import net.minecraftforge.network.NetworkEvent.Context;

public class CollectionAdapter<F, T, CF extends Collection<F>, CT extends Collection<T>> implements NetworkAdapter<CF, CT>
{
	private final IntFunction<CF> fromImplConstructor;
	private final IntFunction<CT> toImplConstructor;
	private final NetworkAdapter<F, T> contentAdapter;
	
	private CollectionAdapter(IntFunction<CF> fromImplConstructor, IntFunction<CT> toImplConstructor, NetworkAdapter<F, T> contentAdapter)
	{
		this.fromImplConstructor = fromImplConstructor;
		this.toImplConstructor = toImplConstructor;
		this.contentAdapter = contentAdapter;
	}

	public static <F, T, CF extends Collection<F>, CT extends Collection<T>> NetworkAdapter<CF, CT> of(IntFunction<CF> fromImplConstructor, IntFunction<CT> toImplConstructor, NetworkAdapter<F, T> contentAdapter)
	{
		return new CollectionAdapter<>(fromImplConstructor, toImplConstructor, contentAdapter);
	}

	@Override
	public CT toNetwork(CF value)
	{
		CT res = this.toImplConstructor.apply(value.size());
		
		for (F content : value)
		{
			res.add(this.contentAdapter.toNetwork(content));
		}
		
		return res;
	}

	@Override
	public CF fromNetwork(CT value, Context ctx)
	{
		CF res = this.fromImplConstructor.apply(value.size());
		
		for (T content : value)
		{
			res.add(this.contentAdapter.fromNetwork(content, ctx));
		}
		
		return res;
	}
}
