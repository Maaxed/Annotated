package fr.max2.annotated.processor.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkEvent;


public class ClassRefTest
{
	
	@Test
	public void testValues()
	{
		assertEquals(INBTSerializable.class.getCanonicalName(), ClassRef.NBT_SERIALIZABLE_INTERFACE.qualifiedName());
		assertEquals(ServerPlayerEntity.class.getCanonicalName(), ClassRef.SERVER_PLAYER.qualifiedName());

		assertEquals(Mod.class.getCanonicalName(), ClassRef.FORGE_MOD_ANNOTATION.qualifiedName());
		assertEquals(NetworkEvent.Context.class.getCanonicalName(), ClassRef.FORGE_NETWORK_CONTEXT.qualifiedName());
	}
	
}
