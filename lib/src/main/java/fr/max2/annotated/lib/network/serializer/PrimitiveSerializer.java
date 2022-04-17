package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public final class PrimitiveSerializer
{
	private PrimitiveSerializer()
	{ }
	
	public static enum ByteSerializer implements NetworkSerializer<Byte>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, byte value)
		{
			buf.writeByte(value);
		}

		public byte decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readByte();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Byte value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Byte decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum ShortSerializer implements NetworkSerializer<Short>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, short value)
		{
			buf.writeShort(value);
		}

		public short decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readShort();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Short value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Short decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum IntSerializer implements NetworkSerializer<Integer>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, int value)
		{
			buf.writeInt(value);
		}

		public int decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readInt();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Integer value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Integer decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum LongSerializer implements NetworkSerializer<Long>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, long value)
		{
			buf.writeLong(value);
		}

		public long decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readLong();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Long value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Long decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum FloatSerializer implements NetworkSerializer<Float>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, float value)
		{
			buf.writeFloat(value);
		}

		public float decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readFloat();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Float value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Float decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum DoubleSerializer implements NetworkSerializer<Double>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, double value)
		{
			buf.writeDouble(value);
		}

		public double decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readDouble();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Double value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Double decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum BooleanSerializer implements NetworkSerializer<Boolean>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, boolean value)
		{
			buf.writeBoolean(value);
		}

		public boolean decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readBoolean();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Boolean value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Boolean decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
	
	public static enum CharSerializer implements NetworkSerializer<Character>
	{
		INSTANCE;

		public void encodePrimitive(FriendlyByteBuf buf, char value)
		{
			buf.writeChar(value);
		}

		public char decodePrimitive(FriendlyByteBuf buf)
		{
			return buf.readChar();
		}

		@Override
		public void encode(FriendlyByteBuf buf, Character value)
		{
			encodePrimitive(buf, value);
		}

		@Override
		public Character decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
}
