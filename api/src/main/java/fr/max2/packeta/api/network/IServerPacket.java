package fr.max2.packeta.api.network;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IServerPacket
{
	void onServerReceive(EntityPlayerMP sender);
	
	default boolean doesServerSynchronize()
	{
		return true;
	}
}
