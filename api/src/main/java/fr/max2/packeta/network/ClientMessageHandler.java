package fr.max2.packeta.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum ClientMessageHandler implements IMessageHandler<IClientMessage, IMessage>
{
	INSTANCE;
	
	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(IClientMessage message, MessageContext ctx)
	{
		if (message.doesClientSynchronize())
			Minecraft.getMinecraft().addScheduledTask(message::onClientReceive);
		else
			message.onClientReceive();
		
		return null;
	}
	
}
