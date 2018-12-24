package fr.max2.packeta.test.network;

import java.util.ArrayList;
import java.util.HashMap;

import fr.max2.packeta.api.processor.network.ConstSize;
import fr.max2.packeta.api.processor.network.CustomData;
import fr.max2.packeta.api.processor.network.DataType;
import fr.max2.packeta.api.processor.network.GeneratePacket;
import fr.max2.packeta.api.lib.network.IClientPacket;
import fr.max2.packeta.api.lib.network.IServerPacket;
import fr.max2.packeta.api.processor.network.IgnoredData;
import fr.max2.packeta.test.util.EnumTest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@GeneratePacket
public class TestData implements IServerPacket, IClientPacket
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
	
	@CustomData(type = DataType.INT)
	public Integer myCustomInt;
	
	public int[] myIntArray;
	public Integer[] myIntWrArray;
	@ConstSize
	public Integer[] myConstSizeIntArray = new Integer[4];

	
	public String[] myStringArray;
	public EnumTest[] myEnumArray;
	public ItemStack[] myStackArray;
	public NBTTagCompound[] myTagArray;
	
	public int[][] myIntArrayArray;

	public ArrayList<Integer> myIntWrList;
	@ConstSize
	public ArrayList<Integer> myConstSizeIntList = new ArrayList<>();

	
	public ArrayList<String> myStringList;
	public ArrayList<EnumTest> myEnumList;
	public ArrayList<ItemStack> myStackList;
	public ArrayList<NBTTagCompound> myTagList;
	
	public HashMap<ItemStack, HashMap<NBTTagByteArray, NBTTagList[]>> stackToList;
	
	@IgnoredData
	public int unusedField;

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
