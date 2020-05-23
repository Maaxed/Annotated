package fr.max2.annotated.processor.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;


public class ClassRefTest
{
	
	@Test
	public void testValues()
	{
		//Minecraft
		assertEquals(INBT.class.getCanonicalName(), ClassRef.NBT_BASE);
		assertEquals(NumberNBT.class.getCanonicalName(), ClassRef.NBT_NUMBER);
		assertEquals(StringNBT.class.getCanonicalName(), ClassRef.NBT_STRING);
		assertEquals(BlockPos.class.getCanonicalName(), ClassRef.BLOCK_POS);
		assertEquals(ItemStack.class.getCanonicalName(), ClassRef.ITEM_STACK);
		assertEquals(ResourceLocation.class.getCanonicalName(), ClassRef.RESOURCE_LOCATION);
		assertEquals(ITextComponent.class.getCanonicalName(), ClassRef.TEXT_COMPONENT);
		assertEquals(BlockRayTraceResult.class.getCanonicalName(), ClassRef.BLOCK_RAY_TRACE);
		assertEquals(Entity.class.getCanonicalName(), ClassRef.ENTITY_BASE);
		assertEquals(PlayerEntity.class.getCanonicalName(), ClassRef.PLAYER_BASE);
		assertEquals(ServerPlayerEntity.class.getCanonicalName(), ClassRef.SERVER_PLAYER);
		//Forge
		assertEquals(IForgeRegistryEntry.class.getCanonicalName(), ClassRef.REGISTRY_ENTRY);
		assertEquals(FluidStack.class.getCanonicalName(), ClassRef.FLUID_STACK);
		assertEquals(INBTSerializable.class.getCanonicalName(), ClassRef.NBT_SERIALIZABLE_INTERFACE);
		assertEquals(Mod.class.getCanonicalName(), ClassRef.FORGE_MOD_ANNOTATION);
		assertEquals(NetworkEvent.Context.class.getCanonicalName(), ClassRef.FORGE_NETWORK_CONTEXT);
	}
	
}
