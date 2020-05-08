package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.UUID;

import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.test.ModTestAnnotated;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

@GenerateChannel(protocolVersion = "1", channelName = ModTestAnnotated.MOD_ID + ":exampledata")
public class ExampleData
{
	@ServerPacket(className = "ExampleMessage")
	public static void doExampleData(int someNumber, ItemStack aStack, String[] aStringArray, ArrayList<UUID> collectionOfIds, ServerPlayerEntity sender)
	{
		sender.sendMessage(new StringTextComponent("The number is " + someNumber));
	}
	
}
