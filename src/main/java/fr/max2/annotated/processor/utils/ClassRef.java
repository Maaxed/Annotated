package fr.max2.annotated.processor.utils;


public class ClassRef
{
	private ClassRef() { }
	
	
	public static final String MAIN_PACKAGE = "fr.max2.annotated";
	
	public static final String NBT_HELPER = MAIN_PACKAGE + ".lib.network.NBTPacketHelper";
	
	public static final String NBT_SERIALIZABLE_INTERFACE = "net.minecraftforge.common.util.INBTSerializable";
	public static final String SERVER_PLAYER = "net.minecraft.entity.player.ServerPlayerEntity";

	public static final String FORGE_MOD_ANNOTATION = "net.minecraftforge.fml.common.Mod";
	public static final String FORGE_NETWORK_CONTEXT = "net.minecraftforge.fml.network.NetworkEvent.Context";
}
