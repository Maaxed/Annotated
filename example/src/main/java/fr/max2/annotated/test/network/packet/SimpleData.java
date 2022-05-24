package fr.max2.annotated.test.network.packet;

import fr.max2.annotated.api.network.ServerPacket;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SimpleData
{
	@ServerPacket
	public static void sendInt(ServerPlayer sender, int myInt)
	{
		sender.sendMessage(new TextComponent("MyInt  is " + myInt), Util.NIL_UUID);
		System.out.println("MyInt is " + myInt);
	}

	@ServerPacket
	public static void sendEntity(Entity myEntity)
	{
		System.out.println("MyEntity !");
	}
}
