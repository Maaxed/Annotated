package fr.max2.annotated.lib.network.serializer;

import net.minecraft.network.FriendlyByteBuf;

public final class PrimitiveSerializer
{
	private PrimitiveSerializer()
	{ }
	
	public static enum ByteSerializer implements NetworkSerializer<Byte>
	{
		INSTANCE;

		public void encodePrimitive(byte value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(short value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(int value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(long value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(float value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(double value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(boolean value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
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

		public void encodePrimitive(char value, FriendlyByteBuf buf)
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
			encodePrimitive(value, buf);
		}

		@Override
		public Character decode(FriendlyByteBuf buf)
		{
			return decodePrimitive(buf);
		}
	}
}
