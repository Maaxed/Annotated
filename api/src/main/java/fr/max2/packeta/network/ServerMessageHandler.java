package fr.max2.packeta.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public enum ServerMessageHandler implements IMessageHandler<IServerMessage, IMessage>
{
	INSTANCE;
	
	@Override
	public IMessage onMessage(IServerMessage message, MessageContext ctx)
	{
		final EntityPlayerMP sender = ctx.getServerHandler().player;
		if (message.doesServerSynchronize())
			sender.getServer().addScheduledTask(() -> message.onServerReceive(sender));
		else
			message.onServerReceive(sender);
		
		return null;
	}
}
