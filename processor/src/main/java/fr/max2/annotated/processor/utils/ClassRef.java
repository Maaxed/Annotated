package fr.max2.annotated.processor.utils;


public class ClassRef
{
	private ClassRef() { }
	
	private static final String MINECRAFT_PACKAGE	= "net.minecraft";
	private static final String FORGE_PACKAGE		= "net.minecraftforge";
	
	
	public static final String
		NBT_BASE			= MINECRAFT_PACKAGE + ".nbt.Tag",
		NBT_NUMBER			= MINECRAFT_PACKAGE + ".nbt.NumericTag",
		NBT_STRING			= MINECRAFT_PACKAGE + ".nbt.StringTag",
		BLOCK_POS			= MINECRAFT_PACKAGE + ".core.BlockPos",
		ITEM_STACK			= MINECRAFT_PACKAGE + ".world.item.ItemStack",
		RESOURCE_LOCATION	= MINECRAFT_PACKAGE + ".resources.ResourceLocation",
		TEXT_COMPONENT		= MINECRAFT_PACKAGE + ".network.chat.Component",
		BLOCK_RAY_TRACE		= MINECRAFT_PACKAGE + ".world.phys.BlockHitResult",
		ENTITY_BASE			= MINECRAFT_PACKAGE + ".world.entity.Entity",
		PLAYER_BASE			= MINECRAFT_PACKAGE + ".world.entity.player.Player",
		SERVER_PLAYER		= MINECRAFT_PACKAGE + ".server.level.ServerPlayer",
		AXIS_ALIGNED_BB		= MINECRAFT_PACKAGE + ".world.phys.AABB",
		MUTABLE_BB			= MINECRAFT_PACKAGE + ".world.level.levelgen.structure.BoundingBox",
		CHUNK_POS			= MINECRAFT_PACKAGE + ".world.level.ChunkPos",
		SECTION_POS			= MINECRAFT_PACKAGE + ".core.SectionPos",
		VECTOR_3D			= MINECRAFT_PACKAGE + ".world.phys.Vec3",
		VECTOR_3I			= MINECRAFT_PACKAGE + ".core.Vec3i";
		

	public static final String
		REGISTRY_ENTRY				= FORGE_PACKAGE + ".registries.IForgeRegistryEntry",
		FLUID_STACK					= FORGE_PACKAGE + ".fluids.FluidStack",
		NBT_SERIALIZABLE_INTERFACE	= FORGE_PACKAGE + ".common.util.INBTSerializable",
		FORGE_MOD_ANNOTATION		= FORGE_PACKAGE + ".fml.common.Mod",
		FORGE_NETWORK_CONTEXT		= FORGE_PACKAGE + ".network.NetworkEvent$Context";
}
