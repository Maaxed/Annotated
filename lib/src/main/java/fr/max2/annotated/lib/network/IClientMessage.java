package fr.max2.annotated.lib.network;

import fr.max2.annotated.api.lib.network.IClientPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// Server -> Client
public interface IClientMessage extends IMessage, IClientPacket
{
	
}
