package fr.max2.packeta.utils;


public class ClassRef
{
	public static final String MAIN_PACKAGE = "fr.max2.packeta";
	
	public static final String NETWORK_ANNOTATION = MAIN_PACKAGE + ".api.network.GenerateNetwork";
	public static final String PACKET_ANNOTATION = MAIN_PACKAGE + ".api.network.GeneratePacket";
	public static final String CONSTSIZE_ANNOTATION = MAIN_PACKAGE + ".api.network.ConstSize";
	
	public static final String CLIENT_PACKET = MAIN_PACKAGE + ".api.network.IClientPacket";
	public static final String SERVER_PACKET = MAIN_PACKAGE + ".api.network.IServerPacket";

	public static final String CLIENT_MESSAGE_NAME = "IClientMessage";
	public static final String SERVER_MESSAGE_NAME = "IServerMessage";
	public static final String CLIENT_MESSAGE = MAIN_PACKAGE + ".network." + CLIENT_MESSAGE_NAME;
	public static final String SERVER_MESSAGE = MAIN_PACKAGE + ".network." + SERVER_MESSAGE_NAME;
	
	public static final String FORGE_MOD_ANNOTATION = "net.minecraftforge.fml.common.Mod";
	public static final String NBT_SERIALIZABLE_INTERFACE = "net.minecraftforge.common.util.INBTSerializable";
}
