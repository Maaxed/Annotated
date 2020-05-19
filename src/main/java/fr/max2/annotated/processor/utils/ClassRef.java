package fr.max2.annotated.processor.utils;


public class ClassRef
{
	private ClassRef() { }
	
	//TODO [v2.0] add refs
	public static final String MAIN_PACKAGE = "fr.max2.annotated";
	
	public static final ClassName NBT_SERIALIZABLE_INTERFACE = new ClassName("net.minecraftforge.common.util", "INBTSerializable");
	public static final ClassName SERVER_PLAYER = new ClassName("net.minecraft.entity.player", "ServerPlayerEntity");

	public static final ClassName FORGE_MOD_ANNOTATION = new ClassName("net.minecraftforge.fml.common", "Mod");
	public static final ClassName FORGE_NETWORK_CONTEXT = new ClassName("net.minecraftforge.fml.network", "NetworkEvent.Context");
}
