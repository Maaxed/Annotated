package fr.max2.annotated.test.network;

import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@GenerateChannel(protocolVersion = "1")
public class SimpleData
{
	@ServerPacket
	public static void onServerReceive(int myInt, ServerPlayerEntity sender)
	{
		sender.sendMessage(new StringTextComponent("The number is " + myInt));
	}
	
}
