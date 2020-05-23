package fr.max2.annotated.processor.utils;


public class ClassRef
{
	private ClassRef() { }
	
	public static final String MAIN_PACKAGE			= "fr.max2.annotated";
	public static final String MINECRAFT_PACKAGE	= "net.minecraft";
	public static final String FORGE_PACKAGE		= "net.minecraftforge";
	
	
	public static final String
		NBT_BASE			= MINECRAFT_PACKAGE + ".nbt.INBT",
		NBT_NUMBER			= MINECRAFT_PACKAGE + ".nbt.NumberNBT",
		NBT_STRING			= MINECRAFT_PACKAGE + ".nbt.StringNBT",
		BLOCK_POS			= MINECRAFT_PACKAGE + ".util.math.BlockPos",
		ITEM_STACK			= MINECRAFT_PACKAGE + ".item.ItemStack",
		RESOURCE_LOCATION	= MINECRAFT_PACKAGE + ".util.ResourceLocation",
		TEXT_COMPONENT		= MINECRAFT_PACKAGE + ".util.text.ITextComponent",
		BLOCK_RAY_TRACE		= MINECRAFT_PACKAGE + ".util.math.BlockRayTraceResult",
		ENTITY_BASE			= MINECRAFT_PACKAGE + ".entity.Entity",
		PLAYER_BASE			= MINECRAFT_PACKAGE + ".entity.player.PlayerEntity",
		SERVER_PLAYER		= MINECRAFT_PACKAGE + ".entity.player.ServerPlayerEntity";

	public static final String
		REGISTRY_ENTRY				= FORGE_PACKAGE + ".registries.IForgeRegistryEntry",
		FLUID_STACK					= FORGE_PACKAGE + ".fluids.FluidStack",
		NBT_SERIALIZABLE_INTERFACE	= FORGE_PACKAGE + ".common.util.INBTSerializable",
		FORGE_MOD_ANNOTATION		= FORGE_PACKAGE + ".fml.common.Mod",
		FORGE_NETWORK_CONTEXT		= FORGE_PACKAGE + ".fml.network.NetworkEvent.Context";
}
