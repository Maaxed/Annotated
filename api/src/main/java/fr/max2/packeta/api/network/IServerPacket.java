package fr.max2.packeta.api.network;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IServerPacket
{
	/**
	 * Called when the packet is received on the server
	 * @param sender the player who sent the packet
	 */
	void onServerReceive(EntityPlayerMP sender);

	/**
	 * Indicate if the execution of the packet on the server side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the server Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	default boolean doesServerSynchronize()
	{
		return true;
	}
}
