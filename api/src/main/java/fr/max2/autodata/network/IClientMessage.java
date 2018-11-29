package fr.max2.autodata.network;

import fr.max2.autodata.api.network.IClientPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// Server -> Client
public interface IClientMessage extends IMessage, IClientPacket
{
	
}
