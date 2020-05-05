package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.UUID;

import fr.max2.annotated.api.processor.network.PacketGenerator;
import fr.max2.annotated.api.processor.network.ServerPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

@PacketGenerator(protocolVersion = "1")
public class ExampleData
{
	@ServerPacket
	public static void doExampleData(int someNumber, ItemStack aStack, String[] aStringArray, ArrayList<UUID> collectionOfIds, ServerPlayerEntity sender)
	{
		sender.sendMessage(new StringTextComponent("The number is " + someNumber));
	}
	
}

