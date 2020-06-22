package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.UUID;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.DelegateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

@DelegateChannel(TestData.class)
public class ExampleData
{
	@ServerPacket(className = "ExampleMessage", runInMainThread = false)
	public static void doExampleData(int someNumber, ItemStack aStack, String[] aStringArray, ArrayList<UUID> collectionOfIds, ServerPlayerEntity sender)
	{
		sender.sendMessage(new StringTextComponent("The number is " + someNumber));
	}
	
	@ClientPacket(className = "ExampleMessageClient", runInMainThread = false)
	public static void doExampleClientData(int someNumber, ItemStack aStack, String[] aStringArray, ArrayList<UUID> collectionOfIds, NetworkEvent.Context context)
	{
		Minecraft.getInstance().player.sendMessage(new StringTextComponent("The number is " + someNumber));
	}
}

