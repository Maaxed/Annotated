package fr.max2.annotated.example.network.packet;

import fr.max2.annotated.api.network.Packet;
import fr.max2.annotated.api.network.Packet.Destination;
import fr.max2.annotated.api.network.Packet.Sender;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SimpleData
{
	@Packet(Destination.SERVER)
	public static void sendInt(@Sender ServerPlayer sender, int myInt)
	{
		sender.sendMessage(new TextComponent("MyInt  is " + myInt), Util.NIL_UUID);
		System.out.println("MyInt is " + myInt);
	}

	@Packet(Destination.SERVER)
	public static void sendEntity(Entity myEntity)
	{
		System.out.println("MyEntity !");
	}
}
