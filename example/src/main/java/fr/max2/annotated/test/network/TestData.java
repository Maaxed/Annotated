package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.CustomData;
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.test.ModTestAnnotated;
import fr.max2.annotated.test.util.EnumTest;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

@GenerateChannel(protocolVersion = "1", channelName = ModTestAnnotated.MOD_ID + ":testdata")
public class TestData
{
	@ServerPacket
	public static void onServerReceive(ServerPlayerEntity sender)
	{
		System.out.println("Packet server");
	}

	@ClientPacket
	public static void onClientReceive()
	{
		System.out.println("Packet client");
	}
	
	@ClientPacket
	public static void integerPrimitives1(byte myByte, short myShort, int myInt, long myLong)
	{
		
	}
	
	@ClientPacket
	public static void integerPrimitives2(Byte myByte, Short myShort, Integer myInt, Long myLong)
	{
		
	}
	
	@ClientPacket
	public static void floatPrimitives1(float myFloat, double myDouble)
	{
		
	}
	
	@ClientPacket
	public static void floatPrimitives2(Float myFloat, Double myDouble)
	{
		
	}
	
	@ClientPacket
	public static void otherPrimitives1(boolean myBoolean, char myChar)
	{
		
	}
	
	@ClientPacket
	public static void otherPrimitives2(Boolean myBoolean, Character myChar)
	{
		
	}
	
	@ClientPacket
	public static void otherJavaObjets(String myString, EnumTest myEnum, UUID id)
	{
		
	}
	
	@ClientPacket
	public static void complexMinecraftObjets(Item item, Block block, ItemStackHandler itemHandler)
	{
		
	}
	
	@ClientPacket
	public static void otherMinecraftObjets(ItemStack myStack, CompoundNBT myTags, ListNBT nyTagList, LongArrayNBT myLongArray, BlockPos pos, ResourceLocation loc, FluidStack fluid, ITextComponent text, BlockRayTraceResult rayResult)
	{
		
	}
	
	@ClientPacket
	public static void primitiveArrays(int[] myIntArray, Integer[] myIntWrArray)
	{
		
	}
	
	@ClientPacket
	public static void objectArrays(String[] myStringArray, EnumTest[] myEnumArray, ItemStack[] myStackArray, CompoundNBT[] myTagArray)
	{
		
	}
	
	@ClientPacket
	public static void nestedArrays(int[][] myIntArrayArray)
	{
		
	}
	
	@ClientPacket
	public static void simpleList(ArrayList<Integer> myIntWrList)
	{
		
	}
	
	@ClientPacket
	public static void objectLists(ArrayList<String> myStringList, ArrayList<EnumTest> myEnumList, ArrayList<ItemStack> myStackList, ArrayList<CompoundNBT> myTagList)
	{
		
	}
	
	@ClientPacket
	public static void simpleMap(HashMap<ItemStack, HashMap<Integer, String[]>> stackToList)
	{
		
	}
	
	@ClientPacket
	public static void specialTypes(ArrayList<? extends Item> items, HashMap<? extends Item, ? extends Block> map)
	{
		
	}
	
	public static class CustomList<A, B> extends ArrayList<A>
	{
		public B unused;
		
		public CustomList()
		{ }
	}
	
	@ClientPacket
	public static void strangeTypes(CustomList<? extends Item, ? extends Item> items, @CustomData({"impl=fr.max2.annotated.test.network.TestData.CustomList"}) List<? extends Item> items2)
	{
		
	}
	
	@ClientPacket
	public static void withCustomData(@CustomData("type=INT") Integer myCustomInt, @CustomData("type=NBT_LIST") ListNBT list1, @CustomData("type=COLLECTION") ListNBT list2)
	{
		
	}
	
	@ClientPacket
	public static void complexCustomData(@CustomData({"impl=java.util.TreeMap", "keys.type=COLLECTION", "values.impl=java.util.ArrayList", "values.content.type=COLLECTION"}) Map<ListNBT, List<ListNBT>> map)
	{
		
	}
	
}
