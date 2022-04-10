package fr.max2.annotated.lib.network.adapter;

import net.minecraftforge.network.NetworkEvent;

public interface NetworkAdapter<F, T>
{
	T toNetwork(F value);
	F fromNetwork(T value, NetworkEvent.Context ctx);
}
