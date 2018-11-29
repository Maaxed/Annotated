package fr.max2.autodata.test.network;

import fr.max2.autodata.api.network.GeneratePacket;
import fr.max2.autodata.api.network.IServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

@GeneratePacket
public class TestData implements IServerPacket
{
	//public byte myByte;
	//public short myShort;
	public int myInt;
	/*public long myLong;
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
	public NBTTagCompound myTag;*/
	

	@Override
	public void onServerReceive(EntityPlayerMP sender)
	{
		sender.sendMessage(new TextComponentString("The number is " + this.myInt));
	}
	
}
