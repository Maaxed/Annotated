package fr.max2.packeta.lib.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class NBTPacketHelper
{
	private NBTPacketHelper() { }
	
	//TODO test the methods (writeNBT / readNBT)
	
	public static void writeNBTByteArray(ByteBuf buffer, @Nullable NBTTagByteArray nbt)
	{
		if (nbt == null)
		{
			buffer.writeInt(-1);
		}
		else
		{
			byte[] values = nbt.getByteArray();
			buffer.writeInt(values.length);
			buffer.writeBytes(values);
		}
	}
	
	@Nullable
	public static NBTTagByteArray readNBTByteArray(ByteBuf buffer)
	{
		int size = buffer.readInt();
		if (size == -1) return null;
		
		byte[] values = new byte[size];
		buffer.readBytes(values);
		return new NBTTagByteArray(values);
	}
	
	@Nonnull
	public static NBTTagByteArray readNBTByteArrayOrEmpty(ByteBuf buffer)
	{
		NBTTagByteArray byteArray = readNBTByteArray(buffer);
		return byteArray == null ? new NBTTagByteArray(new byte[0]) : byteArray;
	}
	
	
	public static void writeNBTIntArray(ByteBuf buffer, @Nullable NBTTagIntArray nbt)
	{
		if (nbt == null)
		{
			buffer.writeInt(-1);
		}
		else
		{
			int[] values = nbt.getIntArray();
			buffer.writeInt(values.length);
			
			for (int value : values)
			{
				buffer.writeInt(value);
			}
		}
	}
	
	@Nullable
	public static NBTTagIntArray readNBTIntArray(ByteBuf buffer)
	{
		int size = buffer.readInt();
		if (size == -1) return null;
		
		int[] values = new int[size];
		
		for (int i = 0; i < size; i++)
		{
			values[i] = buffer.readInt();
		}
		return new NBTTagIntArray(values);
	}
	
	@Nonnull
	public static NBTTagIntArray readNBTIntArrayOrEmpty(ByteBuf buffer)
	{
		NBTTagIntArray intArray = readNBTIntArray(buffer);
		return intArray == null ? new NBTTagIntArray(new int[0]) : intArray;
	}
	
	
	public static void writeNBTList(ByteBuf buffer, @Nullable NBTTagList nbt)
	{
		if (nbt == null)
		{
			buffer.writeByte(-1);
		}
		else
		{
			int type = nbt.getTagType();
			buffer.writeByte(type);
			if (type != 0)
			{
				int size = nbt.tagCount();
				buffer.writeInt(size);
				
				for (int i = 0; i < size; ++i)
				{
					writeNBTData(buffer, nbt.get(i));
				}
			}
		}
	}
	
	@Nullable
	public static NBTTagList readNBTList(ByteBuf buffer)
	{
		int type = buffer.readByte();
		if (type == -1) return null;
		
		NBTTagList nbt = new NBTTagList();
		
		if (type == 0) return nbt;
		
		int i = buffer.readInt();
		
		for (int j = 0; j < i; ++j)
		{
			nbt.appendTag(readNBTData(buffer, type));
		}
		
		return nbt;
	}
	
	@Nonnull
	public static NBTTagList readNBTListOrEmpty(ByteBuf buffer)
	{
		NBTTagList list = readNBTList(buffer);
		return list == null ? new NBTTagList() : list;
	}
	
	
	public static void writeNBTCompound(ByteBuf buffer, @Nullable NBTTagCompound nbt)
	{
		if (nbt == null)
		{
			buffer.writeByte(-1);
		}
		else
		{
			for (String s : nbt.getKeySet())
			{
				NBTBase nbtbase = nbt.getTag(s);
				
				buffer.writeByte(nbtbase.getId());
				
				if (nbtbase.getId() != 0)
				{
					ByteBufUtils.writeUTF8String(buffer, s);
					writeNBTData(buffer, nbtbase);
				}
			}
			
			buffer.writeByte(0);
		}
	}
	
	@Nullable
	public static NBTTagCompound readNBTCompound(ByteBuf buffer)
	{
		byte type = buffer.readByte();
		
		if (type == -1) return null;
		
		NBTTagCompound nbt = new NBTTagCompound();
		
		while (type != 0)
		{
			String key = ByteBufUtils.readUTF8String(buffer);
			NBTBase content = readNBTData(buffer, type);
			
			nbt.setTag(key, content);
			type = buffer.readByte();
		}
		
		return nbt;
	}
	
	@Nonnull
	public static NBTTagCompound readNBTCompoundOrEmpty(ByteBuf buffer)
	{
		NBTTagCompound compound = readNBTCompound(buffer);
		return compound == null ? new NBTTagCompound() : compound;
	}
	
	
	public static void writeNBTPrimitive(ByteBuf buffer, @Nullable NBTPrimitive nbt)
	{
		if (nbt == null) buffer.writeByte(0);
		else
		{
			buffer.writeByte(nbt.getId());
			switch (nbt.getId())
			{
			case NBT.TAG_BYTE:
				buffer.writeByte(nbt.getByte());
				break;
			case NBT.TAG_SHORT:
				buffer.writeShort(nbt.getShort());
				break;
			case NBT.TAG_INT:
				buffer.writeInt(nbt.getInt());
				break;
			case NBT.TAG_LONG:
				buffer.writeLong(nbt.getLong());
				break;
			case NBT.TAG_FLOAT:
				buffer.writeFloat(nbt.getFloat());
				break;
			case NBT.TAG_DOUBLE:
				buffer.writeDouble(nbt.getDouble());
				break;
			default:
				break;
			}
		}
	}
	
	@Nullable
	public static NBTPrimitive readNBTPrimitive(ByteBuf buffer)
	{
		int type = buffer.readByte();
		switch (type)
		{
		case NBT.TAG_BYTE:
			return new NBTTagByte(buffer.readByte());
		case NBT.TAG_SHORT:
			return new NBTTagShort(buffer.readShort());
		case NBT.TAG_INT:
			return new NBTTagInt(buffer.readInt());
		case NBT.TAG_LONG:
			return new NBTTagLong(buffer.readLong());
		case NBT.TAG_FLOAT:
			return new NBTTagFloat(buffer.readFloat());
		case NBT.TAG_DOUBLE:
			return new NBTTagDouble(buffer.readDouble());
		default:
			return null;
		}
	}
	
	
	public static void writeNBTBase(ByteBuf buffer, @Nullable NBTBase nbt)
	{
		if (nbt == null) buffer.writeByte(-1);
		else
		{
			buffer.writeByte(nbt.getId());
			writeNBTData(buffer, nbt);
		}
	}
	
	@Nullable
	public static NBTBase readNBTBase(ByteBuf buffer)
	{
		int type = buffer.readByte();
		if (type == -1) return null;
		return readNBTData(buffer, type);
	}
	
	
	private static void writeNBTData(ByteBuf buffer, @Nonnull NBTBase nbt)
	{
		switch (buffer.readInt())
		{
		case NBT.TAG_BYTE:
			buffer.writeByte(((NBTPrimitive)nbt).getByte());
			break;
		case NBT.TAG_SHORT:
			buffer.writeShort(((NBTPrimitive)nbt).getShort());
			break;
		case NBT.TAG_INT:
			buffer.writeInt(((NBTPrimitive)nbt).getInt());
			break;
		case NBT.TAG_LONG:
			buffer.writeLong(((NBTPrimitive)nbt).getLong());
			break;
		case NBT.TAG_FLOAT:
			buffer.writeFloat(((NBTPrimitive)nbt).getFloat());
			break;
		case NBT.TAG_DOUBLE:
			buffer.writeDouble(((NBTPrimitive)nbt).getDouble());
			break;
			
		case NBT.TAG_STRING:
			ByteBufUtils.writeUTF8String(buffer, ((NBTTagString)nbt).getString());
			break;
			
		case NBT.TAG_BYTE_ARRAY:
			writeNBTByteArray(buffer, (NBTTagByteArray)nbt);
			break;
		case NBT.TAG_INT_ARRAY:
			writeNBTIntArray(buffer, (NBTTagIntArray)nbt);
			break;
			
		case NBT.TAG_LIST:
			writeNBTList(buffer, (NBTTagList)nbt);
			break;
		case NBT.TAG_COMPOUND:
			writeNBTCompound(buffer, (NBTTagCompound)nbt);
			break;
		default:
			break;
		}
	}
	
	@Nonnull
	private static NBTBase readNBTData(ByteBuf buffer, int type)
	{
		switch (buffer.readInt())
		{
		case NBT.TAG_BYTE:
			return new NBTTagByte(buffer.readByte());
		case NBT.TAG_SHORT:
			return new NBTTagShort(buffer.readShort());
		case NBT.TAG_INT:
			return new NBTTagInt(buffer.readInt());
		case NBT.TAG_LONG:
			return new NBTTagLong(buffer.readLong());
		case NBT.TAG_FLOAT:
			return new NBTTagFloat(buffer.readFloat());
		case NBT.TAG_DOUBLE:
			return new NBTTagDouble(buffer.readDouble());
			
		case NBT.TAG_STRING:
			return new NBTTagString(ByteBufUtils.readUTF8String(buffer));
			
		case NBT.TAG_BYTE_ARRAY:
			return readNBTByteArrayOrEmpty(buffer);
		case NBT.TAG_INT_ARRAY:
			return readNBTIntArrayOrEmpty(buffer);
			
		case NBT.TAG_LIST:
			return readNBTListOrEmpty(buffer);
		case NBT.TAG_COMPOUND:
			return readNBTCompoundOrEmpty(buffer);
		default:
			return new NBTTagEnd();
		}
	}
}
