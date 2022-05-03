package fr.max2.annotated.processor.util;


public class ClassRef
{
	private ClassRef() { }

	private static final String MINECRAFT_PACKAGE	= "net.minecraft";
	private static final String FORGE_PACKAGE		= "net.minecraftforge";

	public static final String
		NBT_BASE			= MINECRAFT_PACKAGE + ".nbt.Tag",
		ENTITY_BASE			= MINECRAFT_PACKAGE + ".world.entity.Entity",
		SERVER_PLAYER		= MINECRAFT_PACKAGE + ".server.level.ServerPlayer";

	public static final String
		REGISTRY_ENTRY				= FORGE_PACKAGE + ".registries.IForgeRegistryEntry",
		NBT_SERIALIZABLE_INTERFACE	= FORGE_PACKAGE + ".common.util.INBTSerializable",
		FORGE_MOD_ANNOTATION		= FORGE_PACKAGE + ".fml.common.Mod",
		FORGE_NETWORK_CONTEXT		= FORGE_PACKAGE + ".network.NetworkEvent$Context";
}
