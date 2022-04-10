package fr.max2.annotated.lib.network.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public class CommonUtils
{
	public static Player getPlayer(NetworkEvent.Context ctx)
	{
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			return ClientUtils.getPlayer(); // Client only call
		}
		return ctx.getSender();
	}
	
	public static Level getLevel(NetworkEvent.Context ctx)
	{
		Player player = getPlayer(ctx); 
		return player == null ? null : player.level;
	}
	
	private static class ClientUtils
	{
		public static Player getPlayer()
		{
			return Minecraft.getInstance().player;
		}
	}
}
