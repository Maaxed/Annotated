package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.UUID;

import fr.max2.annotated.api.lib.network.IServerPacket;
import fr.max2.annotated.api.processor.network.GeneratePacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

@GeneratePacket
public class ExampleData implements IServerPacket
{
	protected int someNumber;
	protected ItemStack aStack;
	protected String[] aStringArray;
	protected ArrayList<UUID> collectionOfIds;

	@Override
	public void onServerReceive(EntityPlayerMP sender)
	{
		sender.sendMessage(new TextComponentString("The number is " + this.someNumber));
	}
	
}

