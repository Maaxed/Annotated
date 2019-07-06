package fr.max2.annotated.test.network;

import fr.max2.annotated.api.lib.network.IServerPacket;
import fr.max2.annotated.api.processor.network.GeneratePacket;
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
