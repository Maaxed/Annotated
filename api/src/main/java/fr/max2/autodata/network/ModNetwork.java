package fr.max2.autodata.network;

import fr.max2.autodata.network.ClientMessageHandler;
import fr.max2.autodata.network.IClientMessage;
import fr.max2.autodata.network.IServerMessage;
import fr.max2.autodata.network.ServerMessageHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork
{
	
	public static final SimpleNetworkWrapper MOD_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("auto_data_handler"/*MyMod.MOD_ID*/);
	
	private static final ServerMessageHandler SERVER_MESSAGE_HANDLER = new ServerMessageHandler();
	private static final ClientMessageHandler CLIENT_MESSAGE_HANDLER = new ClientMessageHandler();
	
	public static void registerPackets()
	{
		//TODO auto register
		/*next();
		registerServer(MessageWritePage.class);
		next();
		registerServer(MessageSendMessenger.class);*/
	}
	
	private static int lastIndex = 0;
	
	public static void next()
	{
		lastIndex++;
	}
	
	public static void registerServer(Class<? extends IServerMessage> messageClass)
	{
		MOD_CHANNEL.registerMessage(SERVER_MESSAGE_HANDLER, messageClass, lastIndex, Side.SERVER);
	}
	
	public static void registerClient(Class<? extends IClientMessage> messageClass)
	{
		MOD_CHANNEL.registerMessage(CLIENT_MESSAGE_HANDLER, messageClass, lastIndex, Side.CLIENT);
	}
	
}
