package fr.max2.annotated.test.network.packet;

import fr.max2.annotated.api.network.ServerPacket;
import net.minecraft.world.entity.Entity;

public class SimpleData
{
	@ServerPacket
	public static void simpleData1(int myInt)
	{
		System.out.println("MyInt" + myInt);
	}

	@ServerPacket
	public static void simpleData2(Entity myEntity)
	{
		System.out.println("MyEntity !");
	}
}
