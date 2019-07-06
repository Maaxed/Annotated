package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.HashMap;

import fr.max2.annotated.api.lib.network.IClientPacket;
import fr.max2.annotated.api.lib.network.IServerPacket;
import fr.max2.annotated.api.processor.network.ConstSize;
import fr.max2.annotated.api.processor.network.CustomData;
import fr.max2.annotated.api.processor.network.DataType;
import fr.max2.annotated.api.processor.network.GeneratePacket;
import fr.max2.annotated.api.processor.network.IgnoredData;
import fr.max2.annotated.test.util.EnumTest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@GeneratePacket
public class TestData implements IServerPacket, IClientPacket
{
	protected byte myByte;
	protected short myShort;
	protected int myInt;
	protected long myLong;
	protected Byte myWrByte;
	protected Short myWrShort;
	protected Integer myWrInt;
	protected Long myWrLong;
	
	protected float myFloat;
	protected double myDouble;
	protected Float myWrFloat;
	protected Double myWrDouble;
	
	protected boolean myBoolean;
	protected char myChar;
	protected Boolean myWrBoolean;
	protected Character myWrChar;
	
	protected String myString;
	protected EnumTest myEnum;
	protected ItemStack myStack;
	protected NBTTagCompound myTag;
	
	@CustomData(type = DataType.INT)
	protected Integer myCustomInt;
	
	protected int[] myIntArray;
	protected Integer[] myIntWrArray;
	@ConstSize
	protected Integer[] myConstSizeIntArray = new Integer[4];

	
	protected String[] myStringArray;
	protected EnumTest[] myEnumArray;
	protected ItemStack[] myStackArray;
	protected NBTTagCompound[] myTagArray;
	
	protected int[][] myIntArrayArray;

	protected ArrayList<Integer> myIntWrList;
	@ConstSize
	protected ArrayList<Integer> myConstSizeIntList = new ArrayList<>();

	
	protected ArrayList<String> myStringList;
	protected ArrayList<EnumTest> myEnumList;
	protected ArrayList<ItemStack> myStackList;
	protected ArrayList<NBTTagCompound> myTagList;
	
	protected HashMap<ItemStack, HashMap<NBTTagByteArray, NBTTagList[]>> stackToList;
	
	@IgnoredData
	protected int unusedField;

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
