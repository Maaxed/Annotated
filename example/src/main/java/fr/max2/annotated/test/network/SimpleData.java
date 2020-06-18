package fr.max2.annotated.test.network;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.test.ModTestAnnotated;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@GenerateChannel(protocolVersion = "1", channelName = ModTestAnnotated.MOD_ID + ":simpledata")
public class SimpleData
{
	@ServerPacket
	public static void onServerReceive(int myInt, ServerPlayerEntity sender)
	{
		sender.sendMessage(new StringTextComponent("Server receive number " + myInt));
		SimpleData_clientNumber.sendTo(sender, myInt);
	}

	@ClientPacket
	@OnlyIn(Dist.CLIENT)
	public static void clientNumber(int myInt)
	{
		Minecraft.getInstance().player.sendMessage(new StringTextComponent("Client receive number " + myInt));
	}
	
}
