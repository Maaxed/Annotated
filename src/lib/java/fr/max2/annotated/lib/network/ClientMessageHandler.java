package fr.max2.annotated.lib.network;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientMessageHandler
{
	public static <MSG> BiConsumer<MSG, Supplier<NetworkEvent.Context>> buildHandler(boolean scheduled, BiConsumer<MSG, NetworkEvent.Context> handler)
	{
		if (scheduled)
		{
			return (msg, ctxSup) ->
			{
				NetworkEvent.Context ctx = ctxSup.get();
				if (ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT)
				{
					ctx.enqueueWork(() -> handler.accept(msg, ctx));
					ctx.setPacketHandled(true);
				}
			};
		}
		else
		{
			return (msg, ctxSup) ->
			{
				NetworkEvent.Context ctx = ctxSup.get();
				if (ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT)
				{
					handler.accept(msg, ctx);
					ctx.setPacketHandled(true);
				}
			};
		}
	}
}