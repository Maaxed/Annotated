package fr.max2.packeta.network;

import fr.max2.packeta.api.network.IClientPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// Server -> Client
public interface IClientMessage extends IMessage, IClientPacket
{
	
}
