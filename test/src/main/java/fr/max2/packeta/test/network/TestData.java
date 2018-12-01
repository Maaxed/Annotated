package fr.max2.packeta.test.network;

import fr.max2.packeta.api.network.GeneratePacket;
import fr.max2.packeta.api.network.IServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

@GeneratePacket
public class TestData implements IServerPacket
{
	public int myInt;

	@Override
	public void onServerReceive(EntityPlayerMP sender)
	{
		sender.sendMessage(new TextComponentString("The number is " + this.myInt));
	}
	
}
