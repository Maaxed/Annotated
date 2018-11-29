package fr.max2.autodata.network;

import fr.max2.autodata.api.network.IServerPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// Client -> Server
public interface IServerMessage extends IMessage, IServerPacket
{
	
}
