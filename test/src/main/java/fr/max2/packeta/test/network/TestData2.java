package fr.max2.packeta.test.network;

import fr.max2.packeta.api.network.GeneratePacket;
import fr.max2.packeta.api.network.IClientPacket;
import fr.max2.packeta.api.network.IServerPacket;
import fr.max2.packeta.test.util.EnumTest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@GeneratePacket
public class TestData2 implements IServerPacket, IClientPacket
{
	public byte myByte;
	public short myShort;
	public int myInt;
	public long myLong;
	public Byte myWrByte;
	public Short myWrShort;
	public Integer myWrInt;
	public Long myWrLong;
	
	public float myFloat;
	public double myDouble;
	public Float myWrFloat;
	public Double myWrDouble;
	
	public boolean myBoolean;
	public char myChar;
	public Boolean myWrBoolean;
	public Character myWrChar;
	
	public String myString;
	public EnumTest myEnum;
	public ItemStack myStack;
	public NBTTagCompound myTag;
	

	@Override
	public void onServerReceive(EntityPlayerMP sender)
	{
		System.out.println("Packet server");
	}


	@Override
	public void onClientReceive()
	{
		System.out.println("Packet client");
	}
	
}
