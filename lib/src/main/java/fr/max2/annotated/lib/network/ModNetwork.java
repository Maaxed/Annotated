package fr.max2.annotated.lib.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork
{
	public final SimpleNetworkWrapper modChannel;
	
	public ModNetwork(String channelName)
	{
		modChannel = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
	}
	
	private int lastIndex = 0;
	
	public <T extends IServerMessage & IClientMessage> void registerBothSides(Class<T> messageClass)
	{
		lastIndex++;
		modChannel.registerMessage(ClientMessageHandler.INSTANCE, messageClass, lastIndex, Side.CLIENT);
		modChannel.registerMessage(ServerMessageHandler.INSTANCE, messageClass, lastIndex, Side.SERVER);
	}
	
	public void registerServer(Class<? extends IServerMessage> messageClass)
	{
		lastIndex++;
		modChannel.registerMessage(ServerMessageHandler.INSTANCE, messageClass, lastIndex, Side.SERVER);
	}
	
	public void registerClient(Class<? extends IClientMessage> messageClass)
	{
		lastIndex++;
		modChannel.registerMessage(ClientMessageHandler.INSTANCE, messageClass, lastIndex, Side.CLIENT);
	}
	
}
