package fr.max2.autodata.api.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClientPacket
{
	@SideOnly(Side.CLIENT)
	void onClientReceive();

	@SideOnly(Side.CLIENT)
	default boolean doesClientSynchronize()
	{
		return true;
	}
}
