package fr.max2.packeta.lib.network;

import fr.max2.packeta.api.lib.network.IServerPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// Client -> Server
public interface IServerMessage extends IMessage, IServerPacket
{
	
}
