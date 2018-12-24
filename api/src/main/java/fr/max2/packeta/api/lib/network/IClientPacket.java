package fr.max2.packeta.api.lib.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClientPacket
{
	/**
	 * Called when the packet is received on the client
	 */
	@SideOnly(Side.CLIENT)
	void onClientReceive();
	
	/**
	 * Indicate if the execution of the packet on the client side should be performed in a scheduled task.
	 * If set to false, the execution is performed in the netty Thread.
	 * Only set this to false if you are 100% sure you don't need scheduled task and you don't need to be on the client Thread.
	 * @return true if the execution should be performed in a scheduled task, false otherwise
	 */
	@SideOnly(Side.CLIENT)
	default boolean doesClientSynchronize()
	{
		return true;
	}
}
