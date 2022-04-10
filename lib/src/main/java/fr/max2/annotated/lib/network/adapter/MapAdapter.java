package fr.max2.annotated.lib.network.adapter;

import java.util.Map;
import java.util.function.IntFunction;

import net.minecraftforge.network.NetworkEvent.Context;

public class MapAdapter<FK, FV, TK, TV, FM extends Map<FK, FV>, TM extends Map<TK, TV>> implements NetworkAdapter<FM, TM>
{
	private final IntFunction<FM> fromImplConstructor;
	private final IntFunction<TM> toImplConstructor;
	private final NetworkAdapter<FK, TK> keyAdapter;
	private final NetworkAdapter<FV, TV> valueAdapter;

	private MapAdapter(IntFunction<FM> fromImplConstructor, IntFunction<TM> toImplConstructor, NetworkAdapter<FK, TK> keyAdapter, NetworkAdapter<FV, TV> valueAdapter)
	{
		this.fromImplConstructor = fromImplConstructor;
		this.toImplConstructor = toImplConstructor;
		this.keyAdapter = keyAdapter;
		this.valueAdapter = valueAdapter;
	}

	public static <FK, FV, TK, TV, FM extends Map<FK, FV>, TM extends Map<TK, TV>> NetworkAdapter<FM, TM> of(IntFunction<FM> fromImplConstructor, IntFunction<TM> toImplConstructor, NetworkAdapter<FK, TK> keyAdapter, NetworkAdapter<FV, TV> valueAdapter)
	{
		return new MapAdapter<>(fromImplConstructor, toImplConstructor, keyAdapter, valueAdapter);
	}

	@Override
	public TM toNetwork(FM value)
	{
		TM res = this.toImplConstructor.apply(value.size());
		
		value.forEach((key, val) ->
		{
			res.put(this.keyAdapter.toNetwork(key), this.valueAdapter.toNetwork(val));
		});
		
		return res;
	}

	@Override
	public FM fromNetwork(TM value, Context ctx)
	{
		FM res = this.fromImplConstructor.apply(value.size());
		
		value.forEach((key, val) ->
		{
			res.put(this.keyAdapter.fromNetwork(key, ctx), this.valueAdapter.fromNetwork(val, ctx));
		});
		
		return res;
	}
	
}
