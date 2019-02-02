package fr.max2.packeta.processor.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.max2.packeta.api.processor.network.ConstSize;
import fr.max2.packeta.api.processor.network.GenerateNetwork;
import fr.max2.packeta.api.processor.network.GeneratePacket;
import fr.max2.packeta.lib.network.IClientMessage;
import fr.max2.packeta.lib.network.IServerMessage;
import fr.max2.packeta.lib.network.NBTPacketHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;

public class ClassRefTest
{
	
	@Test
	public void testValues()
	{
		assertEquals(GenerateNetwork.class.getCanonicalName(), ClassRef.NETWORK_ANNOTATION);
		assertEquals(GeneratePacket.class.getCanonicalName(), ClassRef.PACKET_ANNOTATION);
		assertEquals(ConstSize.class.getCanonicalName(), ClassRef.CONSTSIZE_ANNOTATION);

		assertEquals(IClientMessage.class.getSimpleName(), ClassRef.CLIENT_MESSAGE_NAME);
		assertEquals(IServerMessage.class.getSimpleName(), ClassRef.SERVER_MESSAGE_NAME);
		assertEquals(IClientMessage.class.getCanonicalName(), ClassRef.CLIENT_MESSAGE);
		assertEquals(IServerMessage.class.getCanonicalName(), ClassRef.SERVER_MESSAGE);

		assertEquals(NBTPacketHelper.class.getCanonicalName(), ClassRef.NBT_HELPER);
		
		assertEquals(Mod.class.getCanonicalName(), ClassRef.FORGE_MOD_ANNOTATION);
		assertEquals(INBTSerializable.class.getCanonicalName(), ClassRef.NBT_SERIALIZABLE_INTERFACE);
	}
	
}
