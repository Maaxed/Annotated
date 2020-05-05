package fr.max2.annotated.lib.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTypes;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.network.PacketBuffer;

public class NBTPacketHelper
{
	private NBTPacketHelper() { }
	
	//TODO [v1.0] test the methods (writeNBT / readNBT)
	
	@Nullable
	public static ByteArrayNBT readByteArray(PacketBuffer buffer)
	{
		return readNBT(buffer, ByteArrayNBT.class);
	}
	
	@Nonnull
	public static ByteArrayNBT readByteArrayOrEmpty(PacketBuffer buffer)
	{
		ByteArrayNBT byteArray = readByteArray(buffer);
		return byteArray == null ? new ByteArrayNBT(new byte[0]) : byteArray;
	}
	
	
	@Nullable
	public static IntArrayNBT readIntArray(PacketBuffer buffer)
	{
		return readNBT(buffer, IntArrayNBT.class);
	}
	
	@Nonnull
	public static IntArrayNBT readIntArrayOrEmpty(PacketBuffer buffer)
	{
		IntArrayNBT intArray = readIntArray(buffer);
		return intArray == null ? new IntArrayNBT(new int[0]) : intArray;
	}
	
	
	@Nullable
	public static ListNBT readList(PacketBuffer buffer)
	{
		return readNBT(buffer, ListNBT.class);
	}
	
	@Nonnull
	public static ListNBT readListOrEmpty(PacketBuffer buffer)
	{
		ListNBT list = readList(buffer);
		return list == null ? new ListNBT() : list;
	}
	
	
	@Nullable
	public static CompoundNBT readCompound(PacketBuffer buffer)
	{
		return readNBT(buffer, CompoundNBT.class);
	}
	
	@Nonnull
	public static CompoundNBT readCompoundOrEmpty(PacketBuffer buffer)
	{
		CompoundNBT compound = readCompound(buffer);
		return compound == null ? new CompoundNBT() : compound;
	}
	
	
	@Nullable
	public static NumberNBT readNumber(PacketBuffer buffer)
	{
		return readNBT(buffer, NumberNBT.class);
	}
	
	
	public static void writeNBT(PacketBuffer buffer, @Nullable INBT nbt)
	{
		if (nbt == null) {
			buffer.writeByte(0);
			} else {
				try {
					DataOutput output = new ByteBufOutputStream(buffer);
					output.writeByte(nbt.getId());
					if (nbt.getId() != 0) {
						output.writeUTF("");
						nbt.write(output);
					}
				} catch (IOException ioexception) {
					throw new EncoderException(ioexception);
				}
			}
	}
	
	@Nullable
	public static <T extends INBT> T readNBT(PacketBuffer buffer, Class<T> expectedType)
	{
		int i = buffer.readerIndex();
		byte b0 = buffer.readByte();
		if (b0 == 0)
		{
			return null;
		}
		else
		{
			buffer.readerIndex(i);

			try
			{
				INBT inbt = read(new ByteBufInputStream(buffer), 0, new NBTSizeTracker(2097152L));
				if (expectedType.isInstance(inbt))
				{
					return expectedType.cast(inbt);
				}
				else
				{
					throw new IOException("Root tag must be a '" + expectedType.toString() + "' tag");
				}
			}
			catch (IOException ioexception)
			{
				throw new EncoderException(ioexception);
			}
		}
	}

	private static INBT read(DataInput input, int depth, NBTSizeTracker accounter) throws IOException {
		byte b0 = input.readByte();
		accounter.read(8); // Forge: Count everything!
		if (b0 == 0)
		{
			return EndNBT.INSTANCE;
		}
		else
		{
			accounter.readUTF(input.readUTF()); //Forge: Count this string.
			accounter.read(32); //Forge: 4 extra bytes for the object allocation.
			
			try
			{
				return NBTTypes.func_229710_a_(b0).func_225649_b_(input, depth, accounter);
			}
			catch (IOException ioexception)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
				crashreportcategory.addDetail("Tag type", b0);
				throw new ReportedException(crashreport);
			}
		}
	}
}
