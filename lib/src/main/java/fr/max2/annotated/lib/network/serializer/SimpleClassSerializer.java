package fr.max2.annotated.lib.network.serializer;

import java.util.BitSet;
import java.util.Date;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;

public final class SimpleClassSerializer
{
	private SimpleClassSerializer()
	{ }
	
	// TODO [v3.1] Add maxLength parameter
	public static final NetworkSerializer<String> STRING = new DelegatedSerializer<>(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);

	public static final NetworkSerializer<UUID> UUID = new DelegatedSerializer<>(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
	
	public static final NetworkSerializer<Date> DATE = new DelegatedSerializer<>(FriendlyByteBuf::writeDate, FriendlyByteBuf::readDate);
	
	public static final NetworkSerializer<BlockPos> BLOCK_POS = new DelegatedSerializer<>(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
	
	public static final NetworkSerializer<ChunkPos> CHUNK_POS = new DelegatedSerializer<>(FriendlyByteBuf::writeChunkPos, FriendlyByteBuf::readChunkPos);
	
	public static final NetworkSerializer<SectionPos> SECTION_POS = new DelegatedSerializer<>(FriendlyByteBuf::writeSectionPos, FriendlyByteBuf::readSectionPos);
	
	public static final NetworkSerializer<ResourceLocation> RESOURCE_LOCATION = new DelegatedSerializer<>(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);

	public static final NetworkSerializer<ItemStack> ITEM_STACK = new DelegatedSerializer<>(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);

	public static final NetworkSerializer<FluidStack> FLUID_STACK = new DelegatedSerializer<>(FriendlyByteBuf::writeFluidStack, FriendlyByteBuf::readFluidStack);

	// TODO [v3.1] allow serializing specific implementations
	public static final NetworkSerializer<Component> TEXT_COMPONENT = new DelegatedSerializer<>(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);

	public static final NetworkSerializer<BlockHitResult> BLOCK_HIT_RESULT = new DelegatedSerializer<>(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult);

	public static final NetworkSerializer<BitSet> BITSET = new DelegatedSerializer<>(FriendlyByteBuf::writeBitSet, FriendlyByteBuf::readBitSet);
}
