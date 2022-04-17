package fr.max2.annotated.lib.network.serializer;

import java.io.IOException;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.network.FriendlyByteBuf;

public final class TagSerializer
{
	private TagSerializer()
	{ }
	
	public static final class Concrete<T extends Tag> implements NetworkSerializer<T>
	{
		private final TagType<T> tagType;
		
		private Concrete(TagType<T> tagType)
		{
			this.tagType = tagType;
		}
		
		public static <T extends Tag> NetworkSerializer<T> of(TagType<T> tagType)
		{
			// TODO [v3.0] Cache instances
			return new Concrete<>(tagType);
		}

		@Override
		public void encode(FriendlyByteBuf buf, T value)
		{
			concreteEncode(buf, value);
		}
		
		@Override
		public T decode(FriendlyByteBuf buf)
		{
			return concreteDecode(buf, this.tagType);
		}
	};
	
	public static final NetworkSerializer<Tag> Abstract = new NetworkSerializer<>()
	{
		@Override
		public void encode(FriendlyByteBuf buf, Tag value)
		{
        	buf.writeByte(value.getId());
			concreteEncode(buf, value);
		}
		
		@Override
		public Tag decode(FriendlyByteBuf buf)
		{
			byte typeId = buf.readByte();
			return concreteDecode(buf, TagTypes.getType(typeId));
		}
	};

	public static <T extends Tag> void concreteEncode(FriendlyByteBuf buf, T value)
	{
        try
		{
        	value.write(new ByteBufOutputStream(buf));
		}
		catch (IOException e)
		{
            throw new EncoderException(e);
		}
	}
	
	public static <T extends Tag> T concreteDecode(FriendlyByteBuf buf, TagType<T> tagType)
	{
		try
		{
			return tagType.load(new ByteBufInputStream(buf), 0, new NbtAccounter(2097112L));
		}
		catch (IOException e)
		{
            CrashReport report = CrashReport.forThrowable(e, "Loading NBT data");
            report.addCategory("NBT Tag").setDetail("Tag type", tagType.getName());
            throw new ReportedException(report);
		}
	}
}
