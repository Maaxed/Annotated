package fr.max2.packeta.test.network;

import fr.max2.packeta.api.processor.network.GeneratePacket;
import fr.max2.packeta.api.lib.network.IServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

@GeneratePacket
public class SimpleData implements IServerPacket
{
	protected int myInt;

	@Override
	public void onServerReceive(EntityPlayerMP sender)
	{
		sender.sendMessage(new TextComponentString("The number is " + this.myInt));
	}
	
}
