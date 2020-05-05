package fr.max2.annotated.processor.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.max2.annotated.api.processor.network.PacketGenerator;
import fr.max2.annotated.lib.network.NBTPacketHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.NetworkEvent;


public class ClassRefTest
{
	
	@Test
	public void testValues()
	{
		assertEquals(PacketGenerator.class.getCanonicalName(), ClassRef.GENERATOR_ANNOTATION);

		assertEquals(NBTPacketHelper.class.getCanonicalName(), ClassRef.NBT_HELPER);
		
		assertEquals(INBTSerializable.class.getCanonicalName(), ClassRef.NBT_SERIALIZABLE_INTERFACE);
		assertEquals(ServerPlayerEntity.class.getCanonicalName(), ClassRef.SERVER_PLAYER);
		
		assertEquals(NetworkEvent.Context.class.getCanonicalName(), ClassRef.FORGE_NETWORK_CONTEXT);
	}
	
}
