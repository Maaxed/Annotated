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
	
	public static final DelegatedSerializer<String> StringSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);

	public static final DelegatedSerializer<UUID> UUIDSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
	
	public static final DelegatedSerializer<Date> DateSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeDate, FriendlyByteBuf::readDate);
	
	public static final DelegatedSerializer<BlockPos> BlockPosSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
	
	public static final DelegatedSerializer<ChunkPos> ChunkPosSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeChunkPos, FriendlyByteBuf::readChunkPos);
	
	public static final DelegatedSerializer<SectionPos> SectionPosSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeSectionPos, FriendlyByteBuf::readSectionPos);
	
	public static final DelegatedSerializer<ResourceLocation> ResourceLocationSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);

	public static final DelegatedSerializer<ItemStack> ItemStackSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);

	public static final DelegatedSerializer<FluidStack> FluidStackSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeFluidStack, FriendlyByteBuf::readFluidStack);

	public static final DelegatedSerializer<Component> TextComponentSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);

	public static final DelegatedSerializer<BlockHitResult> BlockHitResultSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult);

	public static final DelegatedSerializer<BitSet> BitSetResultSerializer = new DelegatedSerializer<>(FriendlyByteBuf::writeBitSet, FriendlyByteBuf::readBitSet);
}
